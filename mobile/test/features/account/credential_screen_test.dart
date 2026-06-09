import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:tuja_helper/features/account/data/account_repository.dart';
import 'package:tuja_helper/features/account/presentation/credential_screen.dart';

class MockAccountRepository extends Mock implements AccountRepository {}

void main() {
  late MockAccountRepository mockRepository;

  setUp(() {
    mockRepository = MockAccountRepository();
  });

  Widget buildWidget() {
    return ProviderScope(
      overrides: [
        accountRepositoryProvider.overrideWithValue(mockRepository),
      ],
      child: const MaterialApp(
        home: CredentialScreen(),
      ),
    );
  }

  group('CredentialScreen - 유효성 검사', () {
    testWidgets('빈_필드로_등록_시_에러_메시지_표시', (tester) async {
      await tester.pumpWidget(buildWidget());

      await tester.tap(find.byKey(const Key('registerButton')));
      await tester.pump();

      expect(find.text('API Key를 입력해 주세요'), findsOneWidget);
      expect(find.text('API Secret을 입력해 주세요'), findsOneWidget);
      expect(find.text('계좌번호를 입력해 주세요'), findsOneWidget);
    });

    testWidgets('API_Key만_비어있을때_에러_메시지_표시', (tester) async {
      await tester.pumpWidget(buildWidget());

      await tester.enterText(find.byKey(const Key('appSecretField')), 'secret');
      await tester.enterText(find.byKey(const Key('accountNoField')), '12345678-01');
      await tester.tap(find.byKey(const Key('registerButton')));
      await tester.pump();

      expect(find.text('API Key를 입력해 주세요'), findsOneWidget);
      expect(find.text('API Secret을 입력해 주세요'), findsNothing);
      expect(find.text('계좌번호를 입력해 주세요'), findsNothing);
    });
  });

  group('CredentialScreen - 등록 성공', () {
    testWidgets('등록_성공_시_성공_메시지_표시', (tester) async {
      // backend: ApiResponse<Unit> — void 반환
      when(() => mockRepository.registerCredential(
            appKey: any(named: 'appKey'),
            appSecret: any(named: 'appSecret'),
            accountNo: any(named: 'accountNo'),
          )).thenAnswer((_) async {});

      await tester.pumpWidget(buildWidget());

      await tester.enterText(find.byKey(const Key('appKeyField')), 'test_key');
      await tester.enterText(find.byKey(const Key('appSecretField')), 'test_secret');
      await tester.enterText(find.byKey(const Key('accountNoField')), '12345678-01');

      await tester.tap(find.byKey(const Key('registerButton')));
      await tester.pumpAndSettle();

      expect(find.text('API Key가 등록되었습니다'), findsOneWidget);
    });
  });

  group('CredentialScreen - 등록 실패', () {
    testWidgets('중복_등록_시_SnackBar_에러_메시지_표시', (tester) async {
      when(() => mockRepository.registerCredential(
            appKey: any(named: 'appKey'),
            appSecret: any(named: 'appSecret'),
            accountNo: any(named: 'accountNo'),
          )).thenThrow(ApiException(
        code: 'DUPLICATE_CREDENTIAL',
        message: '이미 등록된 API Key입니다',
        statusCode: 409,
      ));

      await tester.pumpWidget(buildWidget());

      await tester.enterText(find.byKey(const Key('appKeyField')), 'test_key');
      await tester.enterText(find.byKey(const Key('appSecretField')), 'test_secret');
      await tester.enterText(find.byKey(const Key('accountNoField')), '12345678-01');

      await tester.tap(find.byKey(const Key('registerButton')));
      await tester.pumpAndSettle();

      expect(find.text('이미 등록된 API Key입니다'), findsOneWidget);
    });
  });

  group('CredentialScreen - 3개 TextField 존재', () {
    testWidgets('3개_텍스트_필드가_렌더링됨', (tester) async {
      await tester.pumpWidget(buildWidget());

      expect(find.byKey(const Key('appKeyField')), findsOneWidget);
      expect(find.byKey(const Key('appSecretField')), findsOneWidget);
      expect(find.byKey(const Key('accountNoField')), findsOneWidget);
    });
  });
}
