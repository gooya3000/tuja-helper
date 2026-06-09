class AccountInfo {
  final String accountNo;
  final String accountName;

  AccountInfo({required this.accountNo, required this.accountName});

  factory AccountInfo.fromJson(Map<String, dynamic> json) => AccountInfo(
        accountNo: json['accountNo'] as String,
        accountName: json['accountName'] as String,
      );
}

class HoldingItem {
  final String stockCode;
  final String stockName;
  final int quantity;
  final String evaluationAmount;

  HoldingItem({
    required this.stockCode,
    required this.stockName,
    required this.quantity,
    required this.evaluationAmount,
  });

  factory HoldingItem.fromJson(Map<String, dynamic> json) => HoldingItem(
        stockCode: json['stockCode'] as String,
        stockName: json['stockName'] as String,
        quantity: json['quantity'] as int,
        evaluationAmount: json['evaluationAmount'] as String,
      );
}

class BalanceData {
  final String totalEvaluationAmount;
  final String depositAmount;
  final List<HoldingItem> holdings;

  BalanceData({
    required this.totalEvaluationAmount,
    required this.depositAmount,
    required this.holdings,
  });

  factory BalanceData.fromJson(Map<String, dynamic> json) => BalanceData(
        totalEvaluationAmount: json['totalEvaluationAmount'] as String,
        depositAmount: json['depositAmount'] as String,
        holdings: (json['holdings'] as List<dynamic>)
            .map((e) => HoldingItem.fromJson(e as Map<String, dynamic>))
            .toList(),
      );
}

class CredentialResponse {
  final int id;

  CredentialResponse({required this.id});

  factory CredentialResponse.fromJson(Map<String, dynamic> json) =>
      CredentialResponse(id: json['id'] as int);
}
