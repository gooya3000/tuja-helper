import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:tuja_helper/features/account/data/account_repository.dart';
import 'package:tuja_helper/features/account/data/dto/balance_response.dart';
import 'package:tuja_helper/features/account/domain/account_state.dart';
import 'package:tuja_helper/features/account/presentation/account_notifier.dart';
import 'package:tuja_helper/features/account/presentation/balance_screen.dart';
import 'package:tuja_helper/shared/network/api_client.dart';

// ---------------------------------------------------------------------------
// Mock 클래스
// ---------------------------------------------------------------------------

class MockAccountRepository extends Mock implements AccountRepository {}

// ---------------------------------------------------------------------------
// 헬퍼 함수
// ---------------------------------------------------------------------------

Widget buildTestApp({
  required Widget child,
  List<Override> overrides = const [],
}) {
  return ProviderScope(
    overrides: overrides,
    child: MaterialApp(home: child),
  );
}

/// 잔고 현황 화면(BalanceScreen) 위젯 테스트
///
/// Red 단계: BalanceScreen, AccountNotifier 등이 미구현이므로
/// import 단계에서 컴파일 오류가 발생한다.
void main() {
  late MockAccountRepository mockAccountRepository;
  const testAccountNo = '12345678-01';

  setUp(() {
    mockAccountRepository = MockAccountRepository();
  });

  // -------------------------------------------------------------------------
  // 화면 렌더링 테스트
  // -------------------------------------------------------------------------

  group('BalanceScreen - 렌더링', () {
    testWidgets('BalanceScreen_초기_로딩_시_CircularProgressIndicator_표시', (tester) async {
      when(() => mockAccountRepository.fetchBalance(testAccountNo)).thenAnswer(
        (_) async {
          await Future.delayed(const Duration(milliseconds: 100));
          return BalanceResponse(
            totalEvaluationAmount: '10000000',
            depositAmount: '5000000',
            holdings: [],
          );
        },
      );

      await tester.pumpWidget(buildTestApp(
        overrides: [
          accountRepositoryProvider.overrideWithValue(mockAccountRepository),
        ],
        child: BalanceScreen(accountNo: testAccountNo),
      ));

      expect(find.byType(CircularProgressIndicator), findsOneWidget);

      await tester.pumpAndSettle();
    });

    testWidgets('BalanceScreen_잔고_데이터_정상_표시_총평가금액과_예수금_노출', (tester) async {
      when(() => mockAccountRepository.fetchBalance(testAccountNo)).thenAnswer(
        (_) async => BalanceResponse(
          totalEvaluationAmount: '10000000',
          depositAmount: '5000000',
          holdings: [],
        ),
      );

      await tester.pumpWidget(buildTestApp(
        overrides: [
          accountRepositoryProvider.overrideWithValue(mockAccountRepository),
        ],
        child: BalanceScreen(accountNo: testAccountNo),
      ));
      await tester.pumpAndSettle();

      expect(find.textContaining('10,000,000'), findsOneWidget);
      expect(find.textContaining('5,000,000'), findsOneWidget);
    });

    testWidgets('BalanceScreen_총평가금액_레이블_표시', (tester) async {
      when(() => mockAccountRepository.fetchBalance(testAccountNo)).thenAnswer(
        (_) async => BalanceResponse(
          totalEvaluationAmount: '10000000',
          depositAmount: '5000000',
          holdings: [],
        ),
      );

      await tester.pumpWidget(buildTestApp(
        overrides: [
          accountRepositoryProvider.overrideWithValue(mockAccountRepository),
        ],
        child: BalanceScreen(accountNo: testAccountNo),
      ));
      await tester.pumpAndSettle();

      expect(find.text('총 평가금액'), findsOneWidget);
      expect(find.text('예수금'), findsOneWidget);
    });

    testWidgets('BalanceScreen_화면_타이틀_계좌번호_포함', (tester) async {
      when(() => mockAccountRepository.fetchBalance(testAccountNo)).thenAnswer(
        (_) async => BalanceResponse(
          totalEvaluationAmount: '10000000',
          depositAmount: '5000000',
          holdings: [],
        ),
      );

      await tester.pumpWidget(buildTestApp(
        overrides: [
          accountRepositoryProvider.overrideWithValue(mockAccountRepository),
        ],
        child: BalanceScreen(accountNo: testAccountNo),
      ));
      await tester.pumpAndSettle();

      expect(find.text('잔고 현황'), findsOneWidget);
    });
  });

  // -------------------------------------------------------------------------
  // 보유 종목 목록
  // -------------------------------------------------------------------------

  group('BalanceScreen - 보유 종목', () {
    testWidgets('BalanceScreen_보유_종목_없는_경우_빈_화면_메시지_표시', (tester) async {
      when(() => mockAccountRepository.fetchBalance(testAccountNo)).thenAnswer(
        (_) async => BalanceResponse(
          totalEvaluationAmount: '5000000',
          depositAmount: '5000000',
          holdings: [],
        ),
      );

      await tester.pumpWidget(buildTestApp(
        overrides: [
          accountRepositoryProvider.overrideWithValue(mockAccountRepository),
        ],
        child: BalanceScreen(accountNo: testAccountNo),
      ));
      await tester.pumpAndSettle();

      expect(find.text('보유 종목이 없습니다'), findsOneWidget);
    });

    testWidgets('BalanceScreen_보유_종목_있는_경우_종목명과_평가금액_표시', (tester) async {
      when(() => mockAccountRepository.fetchBalance(testAccountNo)).thenAnswer(
        (_) async => BalanceResponse(
          totalEvaluationAmount: '1600000',
          depositAmount: '3000000',
          holdings: [
            Holding(
              stockCode: '005930',
              stockName: '삼성전자',
              quantity: 10,
              evaluationAmount: '700000',
            ),
            Holding(
              stockCode: '000660',
              stockName: 'SK하이닉스',
              quantity: 5,
              evaluationAmount: '900000',
            ),
          ],
        ),
      );

      await tester.pumpWidget(buildTestApp(
        overrides: [
          accountRepositoryProvider.overrideWithValue(mockAccountRepository),
        ],
        child: BalanceScreen(accountNo: testAccountNo),
      ));
      await tester.pumpAndSettle();

      expect(find.text('삼성전자'), findsOneWidget);
      expect(find.text('SK하이닉스'), findsOneWidget);
    });

    testWidgets('BalanceScreen_보유_종목_수량_표시', (tester) async {
      when(() => mockAccountRepository.fetchBalance(testAccountNo)).thenAnswer(
        (_) async => BalanceResponse(
          totalEvaluationAmount: '700000',
          depositAmount: '3000000',
          holdings: [
            Holding(
              stockCode: '005930',
              stockName: '삼성전자',
              quantity: 10,
              evaluationAmount: '700000',
            ),
          ],
        ),
      );

      await tester.pumpWidget(buildTestApp(
        overrides: [
          accountRepositoryProvider.overrideWithValue(mockAccountRepository),
        ],
        child: BalanceScreen(accountNo: testAccountNo),
      ));
      await tester.pumpAndSettle();

      // 수량 '10주' 또는 '10' 이 표시되어야 한다
      expect(find.textContaining('10'), findsWidgets);
    });
  });

  // -------------------------------------------------------------------------
  // 에러 처리
  // -------------------------------------------------------------------------

  group('BalanceScreen - 에러 처리', () {
    testWidgets('BalanceScreen_CREDENTIAL_NOT_FOUND_에러_메시지_표시', (tester) async {
      when(() => mockAccountRepository.fetchBalance(testAccountNo)).thenThrow(
        ApiException(code: 'CREDENTIAL_NOT_FOUND', message: '등록된 증권사 정보가 없습니다', statusCode: 404),
      );

      await tester.pumpWidget(buildTestApp(
        overrides: [
          accountRepositoryProvider.overrideWithValue(mockAccountRepository),
        ],
        child: BalanceScreen(accountNo: testAccountNo),
      ));
      await tester.pumpAndSettle();

      expect(find.text('등록된 증권사 정보가 없습니다'), findsOneWidget);
    });

    testWidgets('BalanceScreen_KIS_API_ERROR_에러_메시지_표시', (tester) async {
      when(() => mockAccountRepository.fetchBalance(testAccountNo)).thenThrow(
        ApiException(code: 'KIS_API_ERROR', message: '한국투자증권 API 오류가 발생했습니다', statusCode: 502),
      );

      await tester.pumpWidget(buildTestApp(
        overrides: [
          accountRepositoryProvider.overrideWithValue(mockAccountRepository),
        ],
        child: BalanceScreen(accountNo: testAccountNo),
      ));
      await tester.pumpAndSettle();

      expect(find.text('한국투자증권 API 오류가 발생했습니다'), findsOneWidget);
    });

    testWidgets('BalanceScreen_에러_시_다시_시도_버튼_표시', (tester) async {
      when(() => mockAccountRepository.fetchBalance(testAccountNo)).thenThrow(
        ApiException(code: 'KIS_API_ERROR', message: '한국투자증권 API 오류가 발생했습니다', statusCode: 502),
      );

      await tester.pumpWidget(buildTestApp(
        overrides: [
          accountRepositoryProvider.overrideWithValue(mockAccountRepository),
        ],
        child: BalanceScreen(accountNo: testAccountNo),
      ));
      await tester.pumpAndSettle();

      expect(find.text('다시 시도'), findsOneWidget);
    });

    testWidgets('BalanceScreen_다시_시도_버튼_클릭_시_잔고_재조회', (tester) async {
      var callCount = 0;
      when(() => mockAccountRepository.fetchBalance(testAccountNo)).thenAnswer((_) async {
        callCount++;
        if (callCount == 1) {
          throw ApiException(code: 'KIS_API_ERROR', message: '한국투자증권 API 오류가 발생했습니다', statusCode: 502);
        }
        return BalanceResponse(
          totalEvaluationAmount: '10000000',
          depositAmount: '5000000',
          holdings: [],
        );
      });

      await tester.pumpWidget(buildTestApp(
        overrides: [
          accountRepositoryProvider.overrideWithValue(mockAccountRepository),
        ],
        child: BalanceScreen(accountNo: testAccountNo),
      ));
      await tester.pumpAndSettle();

      // 에러 상태에서 다시 시도 버튼 클릭
      await tester.tap(find.text('다시 시도'));
      await tester.pumpAndSettle();

      // 재조회 성공 후 데이터 표시
      expect(find.textContaining('10,000,000'), findsOneWidget);
    });
  });
}
