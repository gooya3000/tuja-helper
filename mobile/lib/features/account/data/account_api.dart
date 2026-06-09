import 'package:tuja_helper/shared/network/api_client.dart';
import 'dto/account_dto.dart';

class AccountApi {
  final ApiClient _client;

  AccountApi(this._client);

  Future<CredentialResponse> registerCredential({
    required String appKey,
    required String appSecret,
    required String accountNo,
  }) async {
    final response = await _client.post(
      '/brokerage/credentials',
      data: {
        'appKey': appKey,
        'appSecret': appSecret,
        'accountNo': accountNo,
      },
    );
    return CredentialResponse.fromJson(response['data'] as Map<String, dynamic>);
  }

  Future<List<AccountInfo>> getAccounts() async {
    final response = await _client.get('/accounts');
    final data = response['data'] as Map<String, dynamic>;
    return (data['accounts'] as List<dynamic>)
        .map((e) => AccountInfo.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<BalanceData> getBalance(String accountNo) async {
    final response = await _client.get('/accounts/$accountNo/balance');
    return BalanceData.fromJson(response['data'] as Map<String, dynamic>);
  }
}
