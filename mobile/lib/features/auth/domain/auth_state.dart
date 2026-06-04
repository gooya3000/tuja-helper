abstract class AuthState {}

class AuthStateUnauthenticated extends AuthState {}

class AuthStateAuthenticated extends AuthState {
  final String accessToken;

  AuthStateAuthenticated({required this.accessToken});
}

class AuthStateError extends AuthState {
  final String message;

  AuthStateError({required this.message});
}
