package iuh.fit.se.service;

import iuh.fit.se.dto.request.DepositRequest;
import iuh.fit.se.dto.request.WithdrawRequestDto;
import iuh.fit.se.dto.response.WalletResponse;
import iuh.fit.se.entity.Transaction;
import iuh.fit.se.entity.Wallet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WalletService {
    Wallet createWallet(String userId);
    WalletResponse getWalletByUserId(String userId);
    void deposit(String userId, DepositRequest request);
    void withdraw(String userId, WithdrawRequestDto request);
    Page<Transaction> getTransactionHistory(String userId, Pageable pageable);
}