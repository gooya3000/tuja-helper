import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:tuja_helper/features/auth/data/auth_repository.dart';
import 'package:tuja_helper/features/auth/data/dto/login_request.dart';
import 'package:tuja_helper/features/auth/data/dto/signup_request.dart';
import 'package:tuja_helper/features/auth/domain/auth_state.dart';
import 'package:tuja_helper/shared/storage/token_storage.dart';

class AuthNotifier extends StateNotifier<AuthState> {
  final AuthRepository _authRepository;
  final TokenStorage _tokenStorage;

  AuthNotifier(this._authRepository, this._tokenStorage)
      : super(AuthStateUnauthenticated());

  Future<void> login({required String email, required String password}) async {
    try {
      final response = await _authRepository.login(
        LoginRequest(email: email, password: password),
      );
      await _tokenStorage.saveAccessToken(response.accessToken);
      await _tokenStorage.saveRefreshToken(response.refreshToken);
      state = AuthStateAuthenticated(accessToken: response.accessToken);
    } on ApiException catch (e) {
      state = AuthStateError(message: e.message);
    }
  }

  Future<void> signup({required String email, required String password}) async {
    await _authRepository.signup(
      SignupRequest(email: email, password: password),
    );
    await login(email: email, password: password);
  }

  Future<void> logout() async {
    await _tokenStorage.deleteAll();
    state = AuthStateUnauthenticated();
  }

  Future<void> checkAuthStatus() async {
    final accessToken = await _tokenStorage.getAccessToken();
    final refreshToken = await _tokenStorage.getRefreshToken();
    if (accessToken != null && refreshToken != null) {
      state = AuthStateAuthenticated(accessToken: accessToken);
    } else {
      state = AuthStateUnauthenticated();
    }
  }
}

final authNotifierProvider =
    StateNotifierProvider<AuthNotifier, AuthState>(
  (ref) => AuthNotifier(
    ref.read(authRepositoryProvider),
    ref.read(tokenStorageProvider),
  ),
);
