import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:tuja_helper/features/account/data/account_repository.dart';
import 'package:tuja_helper/features/account/domain/account_notifier.dart';
import 'package:tuja_helper/features/account/domain/account_state.dart';

class MockAccountRepository extends Mock implements AccountRepository {}

void main() {
  late MockAccountRepository mockRepository;
  late ProviderContainer container;

  setUp(() {
    mockRepository = MockAccountRepository();
    container = ProviderContainer(
      overrides: [
        accountRepositoryProvider.overrideWithValue(mockRepository),
      ],
    );
  });

  tearDown(() {
    container.dispose();
  });

  group('AccountNotifier 초기 상태', () {
    test('초기_상태는_initial', () {
      final state = container.read(accountNotifierProvider);
      expect(state, isA<AccountStateInitial>());
    });
  });

  group('AccountNotifier - 잔고 조회', () {
    test('잔고_조회_성공_loaded_상태로_변경', () async {
      // backend BalanceDto: holdings 없음, 4개 수치 필드
      when(() => mockRepository.getBalance(any())).thenAnswer(
        (_) async => BalanceData(
          totalEvaluationAmount: 10000000,
          depositAmount: 5000000,
          totalProfitLossAmount: 500000,
          totalProfitLossRate: 5.0,
        ),
      );

      final notifier = container.read(accountNotifierProvider.notifier);
      await notifier.loadBalance('12345678-01');

      final state = container.read(accountNotifierProvider);
      expect(state, isA<AccountStateLoaded>());
      final loaded = state as AccountStateLoaded;
      expect(loaded.balance.totalEvaluationAmount, equals(10000000));
      expect(loaded.balance.depositAmount, equals(5000000));
      expect(loaded.balance.totalProfitLossAmount, equals(500000));
      expect(loaded.balance.totalProfitLossRate, equals(5.0));
    });

    test('잔고_조회_실패_error_상태로_변경', () async {
      when(() => mockRepository.getBalance(any())).thenThrow(
        ApiException(
          code: 'CREDENTIAL_NOT_FOUND',
          message: '등록된 API Key가 없습니다',
          statusCode: 404,
        ),
      );

      final notifier = container.read(accountNotifierProvider.notifier);
      await notifier.loadBalance('12345678-01');

      final state = container.read(accountNotifierProvider);
      expect(state, isA<AccountStateError>());
      expect((state as AccountStateError).message, isNotEmpty);
    });

    test('잔고_조회_수익률_0인_경우_정상_반환', () async {
      when(() => mockRepository.getBalance(any())).thenAnswer(
        (_) async => BalanceData(
          totalEvaluationAmount: 5000000,
          depositAmount: 5000000,
          totalProfitLossAmount: 0,
          totalProfitLossRate: 0.0,
        ),
      );

      final notifier = container.read(accountNotifierProvider.notifier);
      await notifier.loadBalance('12345678-01');

      final state = container.read(accountNotifierProvider);
      expect(state, isA<AccountStateLoaded>());
      final loaded = state as AccountStateLoaded;
      expect(loaded.balance.totalProfitLossRate, equals(0.0));
    });

    test('잔고_조회_중_loading_상태', () async {
      // 잔고 조회가 비동기임을 확인 — loadBalance 호출 전 initial 상태
      final stateBefore = container.read(accountNotifierProvider);
      expect(stateBefore, isA<AccountStateInitial>());

      when(() => mockRepository.getBalance(any())).thenAnswer(
        (_) async => BalanceData(
          totalEvaluationAmount: 0,
          depositAmount: 0,
          totalProfitLossAmount: 0,
          totalProfitLossRate: 0.0,
        ),
      );

      final notifier = container.read(accountNotifierProvider.notifier);
      final future = notifier.loadBalance('12345678-01');

      // 비동기 호출 직후 loading 상태 확인
      expect(container.read(accountNotifierProvider), isA<AccountStateLoading>());

      await future;
    });
  });

  group('AccountNotifier - API Key 등록', () {
    test('API_Key_등록_성공_initial_상태로_복귀', () async {
      // backend: ApiResponse<Unit> — void 반환
      when(() => mockRepository.registerCredential(
            appKey: any(named: 'appKey'),
            appSecret: any(named: 'appSecret'),
            accountNo: any(named: 'accountNo'),
          )).thenAnswer(
        (_) async {},
      );

      final notifier = container.read(accountNotifierProvider.notifier);
      await notifier.registerCredential(
        appKey: 'test_key',
        appSecret: 'test_secret',
        accountNo: '12345678-01',
      );

      final state = container.read(accountNotifierProvider);
      expect(state, isA<AccountStateInitial>());
    });

    test('API_Key_등록_중복_예외_전파', () async {
      when(() => mockRepository.registerCredential(
            appKey: any(named: 'appKey'),
            appSecret: any(named: 'appSecret'),
            accountNo: any(named: 'accountNo'),
          )).thenThrow(
        ApiException(
          code: 'DUPLICATE_CREDENTIAL',
          message: '이미 등록된 API Key입니다',
          statusCode: 409,
        ),
      );

      final notifier = container.read(accountNotifierProvider.notifier);

      expect(
        () => notifier.registerCredential(
          appKey: 'test_key',
          appSecret: 'test_secret',
          accountNo: '12345678-01',
        ),
        throwsA(isA<ApiException>()
            .having((e) => e.code, 'code', 'DUPLICATE_CREDENTIAL')),
      );
    });
  });

  group('AccountNotifier - 계좌 목록 조회', () {
    test('계좌_목록_조회_성공', () async {
      when(() => mockRepository.getAccounts()).thenAnswer(
        (_) async => [
          AccountInfo(accountNo: '12345678-01', accountName: '주식계좌'),
        ],
      );

      final notifier = container.read(accountNotifierProvider.notifier);
      final accounts = await notifier.getAccounts();

      expect(accounts.length, equals(1));
      expect(accounts.first.accountNo, equals('12345678-01'));
    });
  });
}
