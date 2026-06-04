import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:tuja_helper/shared/network/api_client.dart';
export 'package:tuja_helper/shared/network/api_client.dart' show ApiException;
import 'dto/login_request.dart';
import 'dto/login_response.dart';
import 'dto/signup_request.dart';
import 'dto/signup_response.dart';

class AuthRepository {
  final ApiClient _client;

  AuthRepository(this._client);

  Future<SignupResponse> signup(SignupRequest request) async {
    final response = await _client.post('/auth/signup', data: request.toJson());
    return SignupResponse.fromJson(response['data'] as Map<String, dynamic>);
  }

  Future<LoginResponse> login(LoginRequest request) async {
    final response = await _client.post('/auth/login', data: request.toJson());
    return LoginResponse.fromJson(response['data'] as Map<String, dynamic>);
  }

  Future<String> refresh(String refreshToken) async {
    final response = await _client.post(
      '/auth/refresh',
      data: {'refreshToken': refreshToken},
    );
    return (response['data'] as Map<String, dynamic>)['accessToken'] as String;
  }
}

final authRepositoryProvider = Provider<AuthRepository>(
  (ref) => AuthRepository(ref.read(apiClientProvider)),
);
