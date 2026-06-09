import 'package:tuja_helper/shared/network/api_client.dart';
import 'dto/account_dto.dart';

class AccountApi {
  final ApiClient _client;

  AccountApi(this._client);

  // backend: ApiResponse<Unit> — data는 null/빈 객체이므로 반환값 없음
  Future<void> registerCredential({
    required String appKey,
    required String appSecret,
    required String accountNo,
  }) async {
    await _client.post(
      '/brokerage/credentials',
      data: {
        'appKey': appKey,
        'appSecret': appSecret,
        'accountNo': accountNo,
      },
    );
  }

  // backend: ApiResponse<List<AccountDto>> — data가 직접 배열
  Future<List<AccountInfo>> getAccounts() async {
    final response = await _client.get('/accounts');
    return (response['data'] as List<dynamic>)
        .map((e) => AccountInfo.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<BalanceData> getBalance(String accountNo) async {
    final response = await _client.get('/accounts/$accountNo/balance');
    return BalanceData.fromJson(response['data'] as Map<String, dynamic>);
  }
}
