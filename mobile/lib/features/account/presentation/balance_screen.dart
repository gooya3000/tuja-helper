import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:tuja_helper/features/account/domain/account_notifier.dart';
import 'package:tuja_helper/features/account/domain/account_state.dart';

class BalanceScreen extends ConsumerWidget {
  final String accountNo;

  const BalanceScreen({super.key, required this.accountNo});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final state = ref.watch(accountNotifierProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('잔고 현황')),
      body: _buildBody(context, ref, state),
    );
  }

  Widget _buildBody(BuildContext context, WidgetRef ref, AccountState state) {
    if (state is AccountStateLoading || state is AccountStateInitial) {
      // Initial 상태이면 자동으로 로드 시작
      if (state is AccountStateInitial) {
        WidgetsBinding.instance.addPostFrameCallback((_) {
          ref.read(accountNotifierProvider.notifier).loadBalance(accountNo);
        });
      }
      return const Center(child: CircularProgressIndicator());
    }

    if (state is AccountStateError) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(state.message),
            const SizedBox(height: 16),
            ElevatedButton(
              key: const Key('retryButton'),
              onPressed: () =>
                  ref.read(accountNotifierProvider.notifier).loadBalance(accountNo),
              child: const Text('다시 시도'),
            ),
          ],
        ),
      );
    }

    if (state is AccountStateLoaded) {
      final balance = state.balance;
      return Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('총 평가금액: ${balance.totalEvaluationAmount}'),
            const SizedBox(height: 8),
            Text('예수금: ${balance.depositAmount}'),
            const SizedBox(height: 16),
            const Text('보유 종목', style: TextStyle(fontWeight: FontWeight.bold)),
            const SizedBox(height: 8),
            if (balance.holdings.isEmpty)
              const Text('보유 종목이 없습니다')
            else
              Expanded(
                child: ListView.builder(
                  itemCount: balance.holdings.length,
                  itemBuilder: (context, index) {
                    final item = balance.holdings[index];
                    return ListTile(
                      title: Text(item.stockName),
                      subtitle: Text('수량: ${item.quantity}'),
                      trailing: Text(item.evaluationAmount),
                    );
                  },
                ),
              ),
          ],
        ),
      );
    }

    return const SizedBox.shrink();
  }
}
