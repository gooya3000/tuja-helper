import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:tuja_helper/shared/network/api_client.dart';
export 'package:tuja_helper/shared/network/api_client.dart' show ApiException;
import 'account_api.dart';
import 'dto/account_dto.dart';
export 'dto/account_dto.dart';

abstract class AccountRepository {
  Future<void> registerCredential({
    required String appKey,
    required String appSecret,
    required String accountNo,
  });

  Future<List<AccountInfo>> getAccounts();

  Future<BalanceData> getBalance(String accountNo);
}

class AccountRepositoryImpl implements AccountRepository {
  final AccountApi _api;

  AccountRepositoryImpl(this._api);

  @override
  Future<void> registerCredential({
    required String appKey,
    required String appSecret,
    required String accountNo,
  }) =>
      _api.registerCredential(
        appKey: appKey,
        appSecret: appSecret,
        accountNo: accountNo,
      );

  @override
  Future<List<AccountInfo>> getAccounts() => _api.getAccounts();

  @override
  Future<BalanceData> getBalance(String accountNo) =>
      _api.getBalance(accountNo);
}

final accountApiProvider = Provider<AccountApi>(
  (ref) => AccountApi(ref.read(apiClientProvider)),
);

final accountRepositoryProvider = Provider<AccountRepository>(
  (ref) => AccountRepositoryImpl(ref.read(accountApiProvider)),
);
