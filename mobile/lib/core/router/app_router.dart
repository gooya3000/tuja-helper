import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:tuja_helper/features/auth/domain/auth_state.dart';
import 'package:tuja_helper/features/auth/presentation/auth_notifier.dart';
import 'package:tuja_helper/features/account/presentation/credential_screen.dart';
import 'package:tuja_helper/features/account/presentation/balance_screen.dart';
import '../../features/auth/presentation/login_screen.dart';
import '../../features/auth/presentation/signup_screen.dart';
import '../../features/portfolio/presentation/portfolio_screen.dart';

final appRouterProvider = Provider<GoRouter>((ref) {
  return GoRouter(
    initialLocation: '/login',
    redirect: (context, state) {
      final authState = ref.read(authNotifierProvider);
      final isAuthenticated = authState is AuthStateAuthenticated;
      final location = state.location;
      final isAuthRoute =
          location == '/login' || location == '/signup';

      if (!isAuthenticated && !isAuthRoute) return '/login';
      if (isAuthenticated && isAuthRoute) return '/portfolio';
      return null;
    },
    routes: [
      GoRoute(path: '/login', builder: (_, __) => const LoginScreen()),
      GoRoute(path: '/signup', builder: (_, __) => const SignupScreen()),
      GoRoute(path: '/portfolio', builder: (_, __) => const PortfolioScreen()),
      GoRoute(path: '/credential', builder: (_, __) => const CredentialScreen()),
      GoRoute(
        path: '/balance/:accountNo',
        builder: (_, state) => BalanceScreen(
          accountNo: state.params['accountNo']!,
        ),
      ),
    ],
  );
});
