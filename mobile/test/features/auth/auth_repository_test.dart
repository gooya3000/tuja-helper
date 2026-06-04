import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:tuja_helper/features/auth/data/auth_repository.dart';
import 'package:tuja_helper/features/auth/data/dto/login_request.dart';
import 'package:tuja_helper/features/auth/data/dto/login_response.dart';
import 'package:tuja_helper/features/auth/data/dto/signup_request.dart';
import 'package:tuja_helper/features/auth/data/dto/signup_response.dart';
import 'package:tuja_helper/shared/network/api_client.dart';

class MockApiClient extends Mock implements ApiClient {}

void main() {
  late MockApiClient mockApiClient;
  late AuthRepository authRepository;

  setUp(() {
    mockApiClient = MockApiClient();
    authRepository = AuthRepository(mockApiClient);
  });

  group('AuthRepository - signup', () {
    test('signup_정상_요청_SignupResponse_반환', () async {
      final request = SignupRequest(
        email: 'test@example.com',
        password: 'password123',
      );
      final expectedResponse = SignupResponse(userId: 1);

      when(() => mockApiClient.post(
            '/auth/signup',
            data: any(named: 'data'),
          )).thenAnswer((_) async => {'success': true, 'data': {'userId': 1}});

      final result = await authRepository.signup(request);

      expect(result.userId, equals(1));
    });

    test('signup_중복_이메일_예외_발생', () async {
      final request = SignupRequest(
        email: 'duplicate@example.com',
        password: 'password123',
      );

      when(() => mockApiClient.post(
            '/auth/signup',
            data: any(named: 'data'),
          )).thenThrow(ApiException(code: 'DUPLICATE_EMAIL', message: '이미 사용 중인 이메일입니다', statusCode: 409));

      expect(
        () => authRepository.signup(request),
        throwsA(isA<ApiException>().having((e) => e.code, 'code', 'DUPLICATE_EMAIL')),
      );
    });
  });

  group('AuthRepository - login', () {
    test('login_정상_요청_LoginResponse_반환', () async {
      final request = LoginRequest(
        email: 'test@example.com',
        password: 'password123',
      );

      when(() => mockApiClient.post(
            '/auth/login',
            data: any(named: 'data'),
          )).thenAnswer((_) async => {
            'success': true,
            'data': {
              'accessToken': 'access.token.value',
              'refreshToken': 'refresh.token.value',
            }
          });

      final result = await authRepository.login(request);

      expect(result.accessToken, isNotEmpty);
      expect(result.refreshToken, isNotEmpty);
    });

    test('login_잘못된_자격증명_예외_발생', () async {
      final request = LoginRequest(
        email: 'test@example.com',
        password: 'wrong_password',
      );

      when(() => mockApiClient.post(
            '/auth/login',
            data: any(named: 'data'),
          )).thenThrow(ApiException(code: 'INVALID_CREDENTIALS', message: '이메일 또는 비밀번호가 올바르지 않습니다', statusCode: 401));

      expect(
        () => authRepository.login(request),
        throwsA(isA<ApiException>().having((e) => e.code, 'code', 'INVALID_CREDENTIALS')),
      );
    });
  });

  group('AuthRepository - refresh', () {
    test('refresh_유효한_토큰_새_accessToken_반환', () async {
      when(() => mockApiClient.post(
            '/auth/refresh',
            data: any(named: 'data'),
          )).thenAnswer((_) async => {
            'success': true,
            'data': {'accessToken': 'new.access.token'},
          });

      final result = await authRepository.refresh('valid.refresh.token');

      expect(result, isNotEmpty);
    });

    test('refresh_유효하지_않은_토큰_예외_발생', () async {
      when(() => mockApiClient.post(
            '/auth/refresh',
            data: any(named: 'data'),
          )).thenThrow(ApiException(code: 'INVALID_TOKEN', message: '유효하지 않은 토큰입니다', statusCode: 401));

      expect(
        () => authRepository.refresh('invalid.token'),
        throwsA(isA<ApiException>().having((e) => e.code, 'code', 'INVALID_TOKEN')),
      );
    });
  });
}
