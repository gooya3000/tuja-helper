class SignupResponse {
  final int userId;

  SignupResponse({required this.userId});

  factory SignupResponse.fromJson(Map<String, dynamic> json) =>
      SignupResponse(userId: json['userId'] as int);
}
