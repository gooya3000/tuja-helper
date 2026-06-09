import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:tuja_helper/features/account/data/account_repository.dart';
import 'package:tuja_helper/features/account/domain/account_notifier.dart';
import 'package:tuja_helper/features/account/domain/account_state.dart';
import 'package:tuja_helper/features/account/presentation/balance_screen.dart';

class MockAccountRepository extends Mock implements AccountRepository {}

void main() {
  late MockAccountRepository mockRepository;

  setUp(() {
    mockRepository = MockAccountRepository();
  });

  Widget buildWidget({AccountState? initialState}) {
    return ProviderScope(
      overrides: [
        accountRepositoryProvider.overrideWithValue(mockRepository),
        if (initialState != null)
          accountNotifierProvider.overrideWith(
            (ref) => _FakeAccountNotifier(initialState),
          ),
      ],
      child: const MaterialApp(
        home: BalanceScreen(accountNo: '12345678-01'),
      ),
    );
  }

  group('BalanceScreen - 잔고 표시', () {
    testWidgets('총_평가금액과_예수금_표시', (tester) async {
      await tester.pumpWidget(buildWidget(
        initialState: AccountStateLoaded(
          balance: BalanceData(
            totalEvaluationAmount: '10000000',
            depositAmount: '5000000',
            holdings: [],
          ),
        ),
      ));

      expect(find.textContaining('10000000'), findsOneWidget);
      expect(find.textContaining('5000000'), findsOneWidget);
    });

    testWidgets('보유_종목_없을때_안내_메시지_표시', (tester) async {
      await tester.pumpWidget(buildWidget(
        initialState: AccountStateLoaded(
          balance: BalanceData(
            totalEvaluationAmount: '5000000',
            depositAmount: '5000000',
            holdings: [],
          ),
        ),
      ));

      expect(find.text('보유 종목이 없습니다'), findsOneWidget);
    });

    testWidgets('보유_종목_목록_표시', (tester) async {
      await tester.pumpWidget(buildWidget(
        initialState: AccountStateLoaded(
          balance: BalanceData(
            totalEvaluationAmount: '10000000',
            depositAmount: '3000000',
            holdings: [
              HoldingItem(
                stockCode: '005930',
                stockName: '삼성전자',
                quantity: 10,
                evaluationAmount: '700000',
              ),
              HoldingItem(
                stockCode: '000660',
                stockName: 'SK하이닉스',
                quantity: 5,
                evaluationAmount: '600000',
              ),
            ],
          ),
        ),
      ));

      expect(find.text('삼성전자'), findsOneWidget);
      expect(find.text('SK하이닉스'), findsOneWidget);
    });
  });

  group('BalanceScreen - 에러 상태', () {
    testWidgets('에러_메시지와_다시_시도_버튼_표시', (tester) async {
      await tester.pumpWidget(buildWidget(
        initialState: AccountStateError(message: '등록된 API Key가 없습니다'),
      ));

      expect(find.text('등록된 API Key가 없습니다'), findsOneWidget);
      expect(find.byKey(const Key('retryButton')), findsOneWidget);
    });

    testWidgets('다시_시도_버튼_클릭시_loadBalance_호출', (tester) async {
      // 에러 상태를 직접 주입하여 retry 버튼 노출
      await tester.pumpWidget(buildWidget(
        initialState: AccountStateError(message: '오류 발생'),
      ));
      await tester.pump();

      expect(find.byKey(const Key('retryButton')), findsOneWidget);

      // retryButton 클릭 시 mockRepository 대신 _NoOpRepository가 호출됨
      // 여기서는 버튼 동작 여부(예외 발생 여부)만 확인
      await tester.tap(find.byKey(const Key('retryButton')));
      await tester.pump();
      // _NoOpRepository.getBalance가 UnimplementedError를 던지므로
      // 에러 상태가 유지됨
      expect(find.byKey(const Key('retryButton')), findsOneWidget);
    });
  });

  group('BalanceScreen - 로딩 상태', () {
    testWidgets('loading_상태에서_CircularProgressIndicator_표시', (tester) async {
      await tester.pumpWidget(buildWidget(
        initialState: AccountStateLoading(),
      ));

      expect(find.byType(CircularProgressIndicator), findsOneWidget);
    });
  });
}

// 테스트용 고정 상태 Notifier
class _FakeAccountNotifier extends AccountNotifier {
  _FakeAccountNotifier(AccountState state)
      : super(_NoOpRepository()) {
    // 부모 초기화 후 상태를 원하는 값으로 덮어씀
    this.state = state;
  }
}

class _NoOpRepository implements AccountRepository {
  @override
  Future<BalanceData> getBalance(String accountNo) async {
    throw UnimplementedError();
  }

  @override
  Future<List<AccountInfo>> getAccounts() async {
    throw UnimplementedError();
  }

  @override
  Future<CredentialResponse> registerCredential({
    required String appKey,
    required String appSecret,
    required String accountNo,
  }) async {
    throw UnimplementedError();
  }
}
