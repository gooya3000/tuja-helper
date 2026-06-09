import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:tuja_helper/features/account/data/account_repository.dart';
import 'account_state.dart';

class AccountNotifier extends StateNotifier<AccountState> {
  final AccountRepository _repository;

  AccountNotifier(this._repository) : super(AccountStateInitial());

  Future<void> registerCredential({
    required String appKey,
    required String appSecret,
    required String accountNo,
  }) async {
    state = AccountStateLoading();
    try {
      await _repository.registerCredential(
        appKey: appKey,
        appSecret: appSecret,
        accountNo: accountNo,
      );
      // 등록 성공 후 초기 상태로 복귀 (화면에서 성공 메시지 처리)
      state = AccountStateInitial();
    } on ApiException {
      rethrow;
    }
  }

  Future<void> loadBalance(String accountNo) async {
    state = AccountStateLoading();
    try {
      final balance = await _repository.getBalance(accountNo);
      state = AccountStateLoaded(balance: balance);
    } on ApiException catch (e) {
      state = AccountStateError(message: e.message);
    } catch (e) {
      state = AccountStateError(message: '잔고를 불러오는데 실패했습니다');
    }
  }

  Future<List<AccountInfo>> getAccounts() async {
    return _repository.getAccounts();
  }
}

final accountNotifierProvider =
    StateNotifierProvider<AccountNotifier, AccountState>(
  (ref) => AccountNotifier(ref.read(accountRepositoryProvider)),
);
