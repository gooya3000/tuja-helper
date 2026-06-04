import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

const _baseUrl = 'http://localhost:8080/api/v1';
const _storage = FlutterSecureStorage();

Dio createDio() {
  final dio = Dio(BaseOptions(
    baseUrl: _baseUrl,
    connectTimeout: const Duration(seconds: 10),
    receiveTimeout: const Duration(seconds: 15),
    headers: {'Content-Type': 'application/json'},
  ));

  dio.interceptors.add(_AuthInterceptor(dio));
  return dio;
}

class _AuthInterceptor extends QueuedInterceptorsWrapper {
  _AuthInterceptor(this._dio);

  final Dio _dio;

  @override
  Future<void> onRequest(
    RequestOptions options,
    RequestInterceptorHandler handler,
  ) async {
    final token = await _storage.read(key: 'access_token');
    if (token != null) {
      options.headers['Authorization'] = 'Bearer $token';
    }
    handler.next(options);
  }

  @override
  Future<void> onError(
    DioException err,
    ErrorInterceptorHandler handler,
  ) async {
    if (err.response?.statusCode == 401) {
      final refreshed = await _tryRefresh();
      if (refreshed) {
        // 원래 요청 재시도
        final opts = err.requestOptions;
        final token = await _storage.read(key: 'access_token');
        opts.headers['Authorization'] = 'Bearer $token';
        final response = await _dio.fetch(opts);
        return handler.resolve(response);
      }
    }
    handler.next(err);
  }

  Future<bool> _tryRefresh() async {
    try {
      final refreshToken = await _storage.read(key: 'refresh_token');
      if (refreshToken == null) return false;

      final response = await _dio.post(
        '/auth/refresh',
        data: {'refreshToken': refreshToken},
      );
      final newToken = response.data['data']['accessToken'] as String;
      await _storage.write(key: 'access_token', value: newToken);
      return true;
    } catch (_) {
      await _storage.deleteAll();
      return false;
    }
  }
}
