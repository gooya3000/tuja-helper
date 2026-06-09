import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:tuja_helper/features/account/data/account_repository.dart';
import 'package:tuja_helper/features/account/domain/account_state.dart';
import 'package:tuja_helper/features/account/presentation/account_notifier.dart';
import 'package:tuja_helper/features/account/presentation/credential_screen.dart';
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

/// API Key 입력/등록 화면(CredentialScreen) 위젯 테스트
///
/// Red 단계: CredentialScreen, AccountNotifier 등이 미구현이므로
/// import 단계에서 컴파일 오류가 발생한다.
void main() {
  late MockAccountRepository mockAccountRepository;

  setUp(() {
    mockAccountRepository = MockAccountRepository();
  });

  // -------------------------------------------------------------------------
  // 화면 렌더링 테스트
  // -------------------------------------------------------------------------

  group('CredentialScreen - 렌더링', () {
    testWidgets('CredentialScreen_초기_렌더링_입력_필드_3개와_등록_버튼_표시', (tester) async {
      await tester.pumpWidget(buildTestApp(
        overrides: [
          accountRepositoryProvider.overrideWithValue(mockAccountRepository),
        ],
        child: const CredentialScreen(),
      ));

      expect(find.byType(TextField), findsNWidgets(3));
      expect(find.text('API Key'), findsOneWidget);
      expect(find.text('API Secret'), findsOneWidget);
      expect(find.text('계좌번호'), findsOneWidget);
      expect(find.byType(ElevatedButton), findsOneWidget);
    });

    testWidgets('CredentialScreen_화면_타이틀_표시', (tester) async {
      await tester.pumpWidget(buildTestApp(
        overrides: [
          accountRepositoryProvider.overrideWithValue(mockAccountRepository),
        ],
        child: const CredentialScreen(),
      ));

      expect(find.text('증권사 API Key 등록'), findsOneWidget);
    });
  });

  // -------------------------------------------------------------------------
  // 입력값 유효성 검사
  // -------------------------------------------------------------------------

  group('CredentialScreen - 입력값 유효성', () {
    testWidgets('CredentialScreen_빈_필드로_등록_시도_시_에러_메시지_표시', (tester) async {
      await tester.pumpWidget(buildTestApp(
        overrides: [
          accountRepositoryProvider.overrideWithValue(mockAccountRepository),
        ],
        child: const CredentialScreen(),
      ));

      // 빈 상태에서 등록 버튼 클릭
      await tester.tap(find.byType(ElevatedButton));
      await tester.pumpAndSettle();

      // 유효성 검사 에러 메시지가 표시되어야 한다
      expect(find.text('API Key를 입력해주세요'), findsOneWidget);
    });

    testWidgets('CredentialScreen_모든_필드_입력_후_등록_버튼_활성화', (tester) async {
      await tester.pumpWidget(buildTestApp(
        overrides: [
          accountRepositoryProvider.overrideWithValue(mockAccountRepository),
        ],
        child: const CredentialScreen(),
      ));

      final textFields = find.byType(TextField);
      await tester.enterText(textFields.at(0), 'KISDevKey1234567890');
      await tester.enterText(textFields.at(1), 'KISDevSecret1234567890ABCDEFGHIJ');
      await tester.enterText(textFields.at(2), '12345678-01');
      await tester.pump();

      // 버튼이 활성화(onPressed != null) 상태여야 한다
      final button = tester.widget<ElevatedButton>(find.byType(ElevatedButton));
      expect(button.onPressed, isNotNull);
    });
  });

  // -------------------------------------------------------------------------
  // 등록 성공/실패 시나리오
  // -------------------------------------------------------------------------

  group('CredentialScreen - 등록 성공', () {
    testWidgets('CredentialScreen_등록_성공_시_성공_메시지_표시', (tester) async {
      when(() => mockAccountRepository.registerCredential(
            appKey: any(named: 'appKey'),
            appSecret: any(named: 'appSecret'),
            accountNo: any(named: 'accountNo'),
          )).thenAnswer((_) async => 1);

      await tester.pumpWidget(buildTestApp(
        overrides: [
          accountRepositoryProvider.overrideWithValue(mockAccountRepository),
        ],
        child: const CredentialScreen(),
      ));

      final textFields = find.byType(TextField);
      await tester.enterText(textFields.at(0), 'KISDevKey1234567890');
      await tester.enterText(textFields.at(1), 'KISDevSecret1234567890ABCDEFGHIJ');
      await tester.enterText(textFields.at(2), '12345678-01');
      await tester.pump();

      await tester.tap(find.byType(ElevatedButton));
      await tester.pumpAndSettle();

      // 성공 메시지(SnackBar 또는 안내 텍스트) 표시
      expect(find.text('API Key가 등록되었습니다'), findsOneWidget);
    });
  });

  group('CredentialScreen - 등록 실패', () {
    testWidgets('CredentialScreen_중복_등록_오류_시_에러_SnackBar_표시', (tester) async {
      when(() => mockAccountRepository.registerCredential(
            appKey: any(named: 'appKey'),
            appSecret: any(named: 'appSecret'),
            accountNo: any(named: 'accountNo'),
          )).thenThrow(
        ApiException(code: 'DUPLICATE_CREDENTIAL', message: '이미 등록된 API Key입니다', statusCode: 409),
      );

      await tester.pumpWidget(buildTestApp(
        overrides: [
          accountRepositoryProvider.overrideWithValue(mockAccountRepository),
        ],
        child: const CredentialScreen(),
      ));

      final textFields = find.byType(TextField);
      await tester.enterText(textFields.at(0), 'KISDevKey1234567890');
      await tester.enterText(textFields.at(1), 'KISDevSecret1234567890ABCDEFGHIJ');
      await tester.enterText(textFields.at(2), '12345678-01');
      await tester.pump();

      await tester.tap(find.byType(ElevatedButton));
      await tester.pumpAndSettle();

      expect(find.byType(SnackBar), findsOneWidget);
      expect(find.text('이미 등록된 API Key입니다'), findsOneWidget);
    });

    testWidgets('CredentialScreen_등록_중_로딩_인디케이터_표시', (tester) async {
      when(() => mockAccountRepository.registerCredential(
            appKey: any(named: 'appKey'),
            appSecret: any(named: 'appSecret'),
            accountNo: any(named: 'accountNo'),
          )).thenAnswer((_) async {
        await Future.delayed(const Duration(milliseconds: 100));
        return 1;
      });

      await tester.pumpWidget(buildTestApp(
        overrides: [
          accountRepositoryProvider.overrideWithValue(mockAccountRepository),
        ],
        child: const CredentialScreen(),
      ));

      final textFields = find.byType(TextField);
      await tester.enterText(textFields.at(0), 'KISDevKey1234567890');
      await tester.enterText(textFields.at(1), 'KISDevSecret1234567890ABCDEFGHIJ');
      await tester.enterText(textFields.at(2), '12345678-01');
      await tester.pump();

      await tester.tap(find.byType(ElevatedButton));
      await tester.pump(); // 첫 프레임만 렌더링

      // 로딩 중에 CircularProgressIndicator가 표시되어야 한다
      expect(find.byType(CircularProgressIndicator), findsOneWidget);

      await tester.pumpAndSettle();
    });
  });
}
