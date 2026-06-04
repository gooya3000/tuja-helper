import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:tuja_helper/features/auth/data/auth_repository.dart';
import 'package:tuja_helper/features/auth/data/dto/login_request.dart';
import 'package:tuja_helper/features/auth/data/dto/login_response.dart';
import 'package:tuja_helper/features/auth/data/dto/signup_request.dart';
import 'package:tuja_helper/features/auth/data/dto/signup_response.dart';
import 'package:tuja_helper/features/auth/domain/auth_state.dart';
import 'package:tuja_helper/features/auth/presentation/auth_notifier.dart';
import 'package:tuja_helper/shared/storage/token_storage.dart';

class MockAuthRepository extends Mock implements AuthRepository {}

class MockTokenStorage extends Mock implements TokenStorage {}

class FakeLoginRequest extends Fake implements LoginRequest {}

class FakeSignupRequest extends Fake implements SignupRequest {}

void main() {
  late MockAuthRepository mockAuthRepository;
  late MockTokenStorage mockTokenStorage;
  late ProviderContainer container;

  setUpAll(() {
    registerFallbackValue(FakeLoginRequest());
    registerFallbackValue(FakeSignupRequest());
  });

  setUp(() {
    mockAuthRepository = MockAuthRepository();
    mockTokenStorage = MockTokenStorage();

    container = ProviderContainer(
      overrides: [
        authRepositoryProvider.overrideWithValue(mockAuthRepository),
        tokenStorageProvider.overrideWithValue(mockTokenStorage),
      ],
    );
  });

  tearDown(() {
    container.dispose();
  });

  group('AuthNotifier 초기 상태', () {
    test('초기_상태는_unauthenticated', () {
      final state = container.read(authNotifierProvider);

      expect(state, isA<AuthStateUnauthenticated>());
    });
  });

  group('AuthNotifier - 로그인', () {
    test('로그인_성공_상태가_authenticated로_변경', () async {
      when(() => mockAuthRepository.login(any())).thenAnswer(
        (_) async => LoginResponse(
          accessToken: 'access.token',
          refreshToken: 'refresh.token',
        ),
      );
      when(() => mockTokenStorage.saveAccessToken(any())).thenAnswer((_) async {});
      when(() => mockTokenStorage.saveRefreshToken(any())).thenAnswer((_) async {});

      final notifier = container.read(authNotifierProvider.notifier);
      await notifier.login(email: 'test@example.com', password: 'password123');

      final state = container.read(authNotifierProvider);
      expect(state, isA<AuthStateAuthenticated>());
    });

    test('로그인_실패_error_상태로_변경', () async {
      when(() => mockAuthRepository.login(any())).thenThrow(
        ApiException(code: 'INVALID_CREDENTIALS', message: '이메일 또는 비밀번호가 올바르지 않습니다', statusCode: 401),
      );

      final notifier = container.read(authNotifierProvider.notifier);
      await notifier.login(email: 'test@example.com', password: 'wrong_password');

      final state = container.read(authNotifierProvider);
      expect(state, isA<AuthStateError>());
      expect((state as AuthStateError).message, isNotEmpty);
    });
  });

  group('AuthNotifier - 회원가입', () {
    test('회원가입_성공_로그인_상태로_전환', () async {
      when(() => mockAuthRepository.signup(any())).thenAnswer(
        (_) async => SignupResponse(userId: 1),
      );
      when(() => mockAuthRepository.login(any())).thenAnswer(
        (_) async => LoginResponse(
          accessToken: 'access.token',
          refreshToken: 'refresh.token',
        ),
      );
      when(() => mockTokenStorage.saveAccessToken(any())).thenAnswer((_) async {});
      when(() => mockTokenStorage.saveRefreshToken(any())).thenAnswer((_) async {});

      final notifier = container.read(authNotifierProvider.notifier);
      await notifier.signup(email: 'new@example.com', password: 'password123');

      final state = container.read(authNotifierProvider);
      expect(state, isA<AuthStateAuthenticated>());
    });
  });

  group('AuthNotifier - 로그아웃', () {
    test('로그아웃_unauthenticated로_변경', () async {
      // 먼저 로그인 상태로 만들기
      when(() => mockAuthRepository.login(any())).thenAnswer(
        (_) async => LoginResponse(
          accessToken: 'access.token',
          refreshToken: 'refresh.token',
        ),
      );
      when(() => mockTokenStorage.saveAccessToken(any())).thenAnswer((_) async {});
      when(() => mockTokenStorage.saveRefreshToken(any())).thenAnswer((_) async {});
      when(() => mockTokenStorage.deleteAll()).thenAnswer((_) async {});

      final notifier = container.read(authNotifierProvider.notifier);
      await notifier.login(email: 'test@example.com', password: 'password123');

      // 로그아웃
      await notifier.logout();

      final state = container.read(authNotifierProvider);
      expect(state, isA<AuthStateUnauthenticated>());
    });
  });

  group('AuthNotifier - 앱 시작 시 자동 로그인', () {
    test('저장된_토큰이_있으면_authenticated_상태', () async {
      when(() => mockTokenStorage.getAccessToken()).thenAnswer((_) async => 'saved.access.token');
      when(() => mockTokenStorage.getRefreshToken()).thenAnswer((_) async => 'saved.refresh.token');

      final notifier = container.read(authNotifierProvider.notifier);
      await notifier.checkAuthStatus();

      final state = container.read(authNotifierProvider);
      expect(state, isA<AuthStateAuthenticated>());
    });

    test('저장된_토큰이_없으면_unauthenticated_상태', () async {
      when(() => mockTokenStorage.getAccessToken()).thenAnswer((_) async => null);
      when(() => mockTokenStorage.getRefreshToken()).thenAnswer((_) async => null);

      final notifier = container.read(authNotifierProvider.notifier);
      await notifier.checkAuthStatus();

      final state = container.read(authNotifierProvider);
      expect(state, isA<AuthStateUnauthenticated>());
    });
  });
}
