import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:tuja_helper/features/account/data/account_repository.dart';
import 'package:tuja_helper/features/account/data/account_api.dart';
import 'package:tuja_helper/shared/network/api_client.dart';

class MockApiClient extends Mock implements ApiClient {}

void main() {
  late MockApiClient mockApiClient;
  late AccountApi accountApi;
  late AccountRepositoryImpl repository;

  setUp(() {
    mockApiClient = MockApiClient();
    accountApi = AccountApi(mockApiClient);
    repository = AccountRepositoryImpl(accountApi);
  });

  group('AccountRepository - 계좌 목록 조회', () {
    test('getAccounts_정상_응답_계좌_목록_반환', () async {
      // backend: ApiResponse<List<AccountDto>> — data가 직접 배열
      when(() => mockApiClient.get('/accounts')).thenAnswer(
        (_) async => {
          'success': true,
          'data': [
            {'accountNo': '12345678-01', 'accountName': '주식계좌'},
          ],
        },
      );

      final result = await repository.getAccounts();

      expect(result.length, equals(1));
      expect(result.first.accountNo, equals('12345678-01'));
      expect(result.first.accountName, equals('주식계좌'));
    });

    test('getAccounts_API_오류_예외_발생', () async {
      when(() => mockApiClient.get('/accounts')).thenThrow(
        ApiException(
          code: 'UNKNOWN_ERROR',
          message: '서버 오류',
          statusCode: 500,
        ),
      );

      expect(
        () => repository.getAccounts(),
        throwsA(isA<ApiException>()),
      );
    });
  });

  group('AccountRepository - 잔고 조회', () {
    test('getBalance_정상_응답_잔고_데이터_반환', () async {
      // backend: BalanceDto — totalEvaluationAmount, depositAmount, totalProfitLossAmount, totalProfitLossRate
      when(() => mockApiClient.get('/accounts/12345678-01/balance'))
          .thenAnswer(
        (_) async => {
          'success': true,
          'data': {
            'totalEvaluationAmount': 10000000,
            'depositAmount': 5000000,
            'totalProfitLossAmount': 500000,
            'totalProfitLossRate': 5.0,
          },
        },
      );

      final result = await repository.getBalance('12345678-01');

      expect(result.totalEvaluationAmount, equals(10000000));
      expect(result.depositAmount, equals(5000000));
      expect(result.totalProfitLossAmount, equals(500000));
      expect(result.totalProfitLossRate, equals(5.0));
    });

    test('getBalance_CREDENTIAL_NOT_FOUND_예외_발생', () async {
      when(() => mockApiClient.get('/accounts/12345678-01/balance'))
          .thenThrow(
        ApiException(
          code: 'CREDENTIAL_NOT_FOUND',
          message: '등록된 API Key가 없습니다',
          statusCode: 404,
        ),
      );

      expect(
        () => repository.getBalance('12345678-01'),
        throwsA(isA<ApiException>()
            .having((e) => e.code, 'code', 'CREDENTIAL_NOT_FOUND')),
      );
    });

    test('getBalance_수익률_0인_경우_정상_반환', () async {
      when(() => mockApiClient.get('/accounts/12345678-01/balance'))
          .thenAnswer(
        (_) async => {
          'success': true,
          'data': {
            'totalEvaluationAmount': 5000000,
            'depositAmount': 5000000,
            'totalProfitLossAmount': 0,
            'totalProfitLossRate': 0.0,
          },
        },
      );

      final result = await repository.getBalance('12345678-01');

      expect(result.totalProfitLossAmount, equals(0));
      expect(result.totalProfitLossRate, equals(0.0));
    });
  });

  group('AccountRepository - API Key 등록', () {
    test('registerCredential_정상_응답_완료', () async {
      // backend: ApiResponse<Unit> — data 없음, success: true 만 반환
      when(() => mockApiClient.post(
            '/brokerage/credentials',
            data: any(named: 'data'),
          )).thenAnswer(
        (_) async => {
          'success': true,
        },
      );

      // void 반환이므로 예외 없이 완료되면 성공
      await expectLater(
        repository.registerCredential(
          appKey: 'test_app_key',
          appSecret: 'test_app_secret',
          accountNo: '12345678-01',
        ),
        completes,
      );
    });

    test('registerCredential_중복_DUPLICATE_CREDENTIAL_예외_발생', () async {
      when(() => mockApiClient.post(
            '/brokerage/credentials',
            data: any(named: 'data'),
          )).thenThrow(
        ApiException(
          code: 'DUPLICATE_CREDENTIAL',
          message: '이미 등록된 API Key입니다',
          statusCode: 409,
        ),
      );

      expect(
        () => repository.registerCredential(
          appKey: 'test_app_key',
          appSecret: 'test_app_secret',
          accountNo: '12345678-01',
        ),
        throwsA(isA<ApiException>()
            .having((e) => e.code, 'code', 'DUPLICATE_CREDENTIAL')),
      );
    });
  });
}
