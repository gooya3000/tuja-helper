import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:tuja_helper/features/account/data/account_repository.dart';
import 'package:tuja_helper/features/account/domain/account_notifier.dart';

class CredentialScreen extends ConsumerStatefulWidget {
  const CredentialScreen({super.key});

  @override
  ConsumerState<CredentialScreen> createState() => _CredentialScreenState();
}

class _CredentialScreenState extends ConsumerState<CredentialScreen> {
  final _appKeyController = TextEditingController();
  final _appSecretController = TextEditingController();
  final _accountNoController = TextEditingController();

  String? _appKeyError;
  String? _appSecretError;
  String? _accountNoError;
  bool _isLoading = false;

  @override
  void dispose() {
    _appKeyController.dispose();
    _appSecretController.dispose();
    _accountNoController.dispose();
    super.dispose();
  }

  bool _validate() {
    bool valid = true;
    setState(() {
      _appKeyError = _appKeyController.text.trim().isEmpty ? 'API Key를 입력해 주세요' : null;
      _appSecretError = _appSecretController.text.trim().isEmpty ? 'API Secret을 입력해 주세요' : null;
      _accountNoError = _accountNoController.text.trim().isEmpty ? '계좌번호를 입력해 주세요' : null;
    });
    if (_appKeyError != null || _appSecretError != null || _accountNoError != null) {
      valid = false;
    }
    return valid;
  }

  Future<void> _onRegister() async {
    if (!_validate()) return;

    setState(() => _isLoading = true);
    try {
      await ref.read(accountNotifierProvider.notifier).registerCredential(
            appKey: _appKeyController.text.trim(),
            appSecret: _appSecretController.text.trim(),
            accountNo: _accountNoController.text.trim(),
          );
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('API Key가 등록되었습니다')),
        );
      }
    } on ApiException catch (e) {
      if (!mounted) return;
      if (e.code == 'DUPLICATE_CREDENTIAL') {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(e.message)),
        );
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(e.message)),
        );
      }
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('API Key 등록')),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            TextField(
              key: const Key('appKeyField'),
              controller: _appKeyController,
              decoration: InputDecoration(
                labelText: 'API Key',
                errorText: _appKeyError,
              ),
            ),
            const SizedBox(height: 12),
            TextField(
              key: const Key('appSecretField'),
              controller: _appSecretController,
              decoration: InputDecoration(
                labelText: 'API Secret',
                errorText: _appSecretError,
              ),
              obscureText: true,
            ),
            const SizedBox(height: 12),
            TextField(
              key: const Key('accountNoField'),
              controller: _accountNoController,
              decoration: InputDecoration(
                labelText: '계좌번호',
                errorText: _accountNoError,
              ),
            ),
            const SizedBox(height: 24),
            _isLoading
                ? const Center(child: CircularProgressIndicator())
                : ElevatedButton(
                    key: const Key('registerButton'),
                    onPressed: _onRegister,
                    child: const Text('등록'),
                  ),
          ],
        ),
      ),
    );
  }
}
