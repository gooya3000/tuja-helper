import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:tuja_helper/features/account/data/account_repository.dart';
import 'package:tuja_helper/features/account/data/dto/account_list_response.dart';
import 'package:tuja_helper/features/account/data/dto/balance_response.dart';
import 'package:tuja_helper/features/account/domain/account_state.dart';
import 'package:tuja_helper/features/account/presentation/account_notifier.dart';
import 'package:tuja_helper/shared/network/api_client.dart';

// ---------------------------------------------------------------------------
// Mock 클래스
// ---------------------------------------------------------------------------

class MockAccountRepository extends Mock implements AccountRepository {}

// ---------------------------------------------------------------------------
// 테스트 본문
// ---------------------------------------------------------------------------

/// AccountNotifier 상태 관리 단위 테스트
///
/// Red 단계: AccountRepository, AccountNotifier, 관련 DTO/상태 클래스가
/// 아직 구현되지 않았으므로 컴파일 오류가 발생한다.
void main() {
  late MockAccountRepository mockAccountRepository;
  late ProviderContainer container;

  setUp(() {
    mockAccountRepository = MockAccountRepository();

    container = ProviderContainer(
      overrides: [
        accountRepositoryProvider.overrideWithValue(mockAccountRepository),
      ],
    );
  });

  tearDown(() {
    container.dispose();
  });

  // -------------------------------------------------------------------------
  // 초기 상태
  // -------------------------------------------------------------------------

  group('AccountNotifier 초기 상태', () {
    test('초기_상태는_AccountStateInitial', () {
      final state = container.read(accountNotifierProvider);

      expect(state, isA<AccountStateInitial>());
    });
  });

  // -------------------------------------------------------------------------
  // 계좌 목록 조회
  // -------------------------------------------------------------------------

  group('AccountNotifier - 계좌 목록 조회', () {
    test('계좌_목록_조회_성공_AccountStateLoaded로_변경', () async {
      final accounts = [
        Account(accountNo: '12345678-01', accountName: '위탁계좌'),
        Account(accountNo: '12345678-02', accountName: 'ISA계좌'),
      ];

      when(() => mockAccountRepository.fetchAccounts()).thenAnswer(
        (_) async => AccountListResponse(accounts: accounts),
      );

      final notifier = container.read(accountNotifierProvider.notifier);
      await notifier.loadAccounts();

      final state = container.read(accountNotifierProvider);
      expect(state, isA<AccountStateLoaded>());
      expect((state as AccountStateLoaded).accounts.length, equals(2));
    });

    test('계좌_목록_조회_중_로딩_상태_AccountStateLoading', () async {
      when(() => mockAccountRepository.fetchAccounts()).thenAnswer(
        (_) async {
          await Future.delayed(const Duration(milliseconds: 50));
          return AccountListResponse(accounts: []);
        },
      );

      final notifier = container.read(accountNotifierProvider.notifier);
      final future = notifier.loadAccounts();

      // 로딩 중에는 AccountStateLoading 상태여야 한다
      expect(container.read(accountNotifierProvider), isA<AccountStateLoading>());

      await future;
    });

    test('계좌_목록_조회_실패_AccountStateError로_변경', () async {
      when(() => mockAccountRepository.fetchAccounts()).thenThrow(
        ApiException(code: 'UNKNOWN_ERROR', message: '서버 오류', statusCode: 500),
      );

      final notifier = container.read(accountNotifierProvider.notifier);
      await notifier.loadAccounts();

      final state = container.read(accountNotifierProvider);
      expect(state, isA<AccountStateError>());
      expect((state as AccountStateError).message, isNotEmpty);
    });

    test('계좌_목록_빈_경우_빈_배열_AccountStateLoaded', () async {
      when(() => mockAccountRepository.fetchAccounts()).thenAnswer(
        (_) async => AccountListResponse(accounts: []),
      );

      final notifier = container.read(accountNotifierProvider.notifier);
      await notifier.loadAccounts();

      final state = container.read(accountNotifierProvider);
      expect(state, isA<AccountStateLoaded>());
      expect((state as AccountStateLoaded).accounts, isEmpty);
    });
  });

  // -------------------------------------------------------------------------
  // 잔고 조회
  // -------------------------------------------------------------------------

  group('AccountNotifier - 잔고 조회', () {
    test('잔고_조회_성공_AccountStateBalanceLoaded로_변경', () async {
      const accountNo = '12345678-01';
      final balance = BalanceResponse(
        totalEvaluationAmount: '10000000',
        depositAmount: '5000000',
        holdings: [],
      );

      when(() => mockAccountRepository.fetchBalance(accountNo)).thenAnswer(
        (_) async => balance,
      );

      final notifier = container.read(accountNotifierProvider.notifier);
      await notifier.loadBalance(accountNo);

      final state = container.read(accountNotifierProvider);
      expect(state, isA<AccountStateBalanceLoaded>());
      final balanceState = state as AccountStateBalanceLoaded;
      expect(balanceState.totalEvaluationAmount, equals('10000000'));
      expect(balanceState.depositAmount, equals('5000000'));
    });

    test('잔고_조회_크리덴셜_없음_AccountStateError_CREDENTIAL_NOT_FOUND', () async {
      const accountNo = '99999999-99';

      when(() => mockAccountRepository.fetchBalance(accountNo)).thenThrow(
        ApiException(code: 'CREDENTIAL_NOT_FOUND', message: '등록된 증권사 정보가 없습니다', statusCode: 404),
      );

      final notifier = container.read(accountNotifierProvider.notifier);
      await notifier.loadBalance(accountNo);

      final state = container.read(accountNotifierProvider);
      expect(state, isA<AccountStateError>());
      expect((state as AccountStateError).errorCode, equals('CREDENTIAL_NOT_FOUND'));
    });

    test('잔고_조회_한투_API_오류_AccountStateError_KIS_API_ERROR', () async {
      const accountNo = '12345678-01';

      when(() => mockAccountRepository.fetchBalance(accountNo)).thenThrow(
        ApiException(code: 'KIS_API_ERROR', message: '한국투자증권 API 오류가 발생했습니다', statusCode: 502),
      );

      final notifier = container.read(accountNotifierProvider.notifier);
      await notifier.loadBalance(accountNo);

      final state = container.read(accountNotifierProvider);
      expect(state, isA<AccountStateError>());
      expect((state as AccountStateError).errorCode, equals('KIS_API_ERROR'));
    });

    test('보유_종목이_포함된_잔고_조회_성공', () async {
      const accountNo = '12345678-01';
      final holdings = [
        Holding(stockCode: '005930', stockName: '삼성전자', quantity: 10, evaluationAmount: '700000'),
        Holding(stockCode: '000660', stockName: 'SK하이닉스', quantity: 5, evaluationAmount: '900000'),
      ];
      final balance = BalanceResponse(
        totalEvaluationAmount: '1600000',
        depositAmount: '3000000',
        holdings: holdings,
      );

      when(() => mockAccountRepository.fetchBalance(accountNo)).thenAnswer(
        (_) async => balance,
      );

      final notifier = container.read(accountNotifierProvider.notifier);
      await notifier.loadBalance(accountNo);

      final state = container.read(accountNotifierProvider);
      expect(state, isA<AccountStateBalanceLoaded>());
      expect((state as AccountStateBalanceLoaded).holdings.length, equals(2));
    });
  });

  // -------------------------------------------------------------------------
  // API Key 등록
  // -------------------------------------------------------------------------

  group('AccountNotifier - API Key 등록', () {
    test('credentials_등록_성공_AccountStateCredentialSaved로_변경', () async {
      when(() => mockAccountRepository.registerCredential(
            appKey: any(named: 'appKey'),
            appSecret: any(named: 'appSecret'),
            accountNo: any(named: 'accountNo'),
          )).thenAnswer((_) async => 1);

      final notifier = container.read(accountNotifierProvider.notifier);
      await notifier.registerCredential(
        appKey: 'KISDevKey1234567890',
        appSecret: 'KISDevSecret1234567890ABCDEFGHIJ',
        accountNo: '12345678-01',
      );

      final state = container.read(accountNotifierProvider);
      expect(state, isA<AccountStateCredentialSaved>());
    });

    test('credentials_중복_등록_AccountStateError_DUPLICATE_CREDENTIAL', () async {
      when(() => mockAccountRepository.registerCredential(
            appKey: any(named: 'appKey'),
            appSecret: any(named: 'appSecret'),
            accountNo: any(named: 'accountNo'),
          )).thenThrow(
        ApiException(code: 'DUPLICATE_CREDENTIAL', message: '이미 등록된 API Key입니다', statusCode: 409),
      );

      final notifier = container.read(accountNotifierProvider.notifier);
      await notifier.registerCredential(
        appKey: 'KISDevKey1234567890',
        appSecret: 'KISDevSecret1234567890ABCDEFGHIJ',
        accountNo: '12345678-01',
      );

      final state = container.read(accountNotifierProvider);
      expect(state, isA<AccountStateError>());
      expect((state as AccountStateError).errorCode, equals('DUPLICATE_CREDENTIAL'));
    });
  });
}
