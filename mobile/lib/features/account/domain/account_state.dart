import '../data/dto/account_dto.dart';

abstract class AccountState {}

class AccountStateInitial extends AccountState {}

class AccountStateLoading extends AccountState {}

class AccountStateLoaded extends AccountState {
  final BalanceData balance;

  AccountStateLoaded({required this.balance});
}

class AccountStateError extends AccountState {
  final String message;

  AccountStateError({required this.message});
}
