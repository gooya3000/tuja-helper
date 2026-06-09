class AccountInfo {
  final String accountNo;
  final String accountName;

  AccountInfo({required this.accountNo, required this.accountName});

  factory AccountInfo.fromJson(Map<String, dynamic> json) => AccountInfo(
        accountNo: json['accountNo'] as String,
        accountName: json['accountName'] as String,
      );
}

class BalanceData {
  final num totalEvaluationAmount;
  final num depositAmount;
  final num totalProfitLossAmount;
  final num totalProfitLossRate;

  BalanceData({
    required this.totalEvaluationAmount,
    required this.depositAmount,
    required this.totalProfitLossAmount,
    required this.totalProfitLossRate,
  });

  factory BalanceData.fromJson(Map<String, dynamic> json) => BalanceData(
        totalEvaluationAmount: json['totalEvaluationAmount'] as num,
        depositAmount: json['depositAmount'] as num,
        totalProfitLossAmount: json['totalProfitLossAmount'] as num,
        totalProfitLossRate: json['totalProfitLossRate'] as num,
      );
}
