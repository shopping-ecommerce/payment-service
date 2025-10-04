package iuh.fit.se.service.impl;

import iuh.fit.se.dto.request.DepositRequest;
import iuh.fit.se.dto.request.WithdrawRequestDto;
import iuh.fit.se.dto.response.WalletResponse;
import iuh.fit.se.entity.Transaction;
import iuh.fit.se.entity.Wallet;
import iuh.fit.se.entity.WithdrawRequest;
import iuh.fit.se.entity.enums.Status;
import iuh.fit.se.entity.enums.TransactionType;
import iuh.fit.se.exception.AppException;
import iuh.fit.se.exception.ErrorCode;
import iuh.fit.se.repository.TransactionRepository;
import iuh.fit.se.repository.WalletRepository;
import iuh.fit.se.repository.WithdrawRequestRepository;
import iuh.fit.se.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
@Slf4j
public class WalletServiceImpl implements WalletService {

    WalletRepository walletRepository;
    TransactionRepository transactionRepository;
    WithdrawRequestRepository withdrawRequestRepository;

    @Override
    @Transactional
    public Wallet createWallet(String userId) {
//        if (walletRepository.existsByUserId(userId)) {
//            throw new IllegalArgumentException("Wallet already exists for user: " + userId);
//        }

        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setBalance(BigDecimal.ZERO);

        return walletRepository.save(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getWalletByUserId(String userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return WalletResponse.builder()
                .id(wallet.getId())
                .userId(wallet.getUserId())
                .balance(wallet.getBalance().doubleValue())
                .modifiedAt(wallet.getModifiedAt())
                .build();
    }

    @Override
    @Transactional
    public void deposit(String userId, DepositRequest request) {
        log.info("Initiating deposit for user: {}, amount: {}", userId, request.getAmount());
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Update balance
        wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        walletRepository.save(wallet);

        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setWallet(wallet);
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setType(TransactionType.DEPOSIT);
        transactionRepository.save(transaction);

        log.info("Deposit successful for user: {}, amount: {}", userId, request.getAmount());
    }

    @Override
    @Transactional
    public void withdraw(String userId, WithdrawRequestDto request) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Check balance
        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // Deduct balance
        wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
        walletRepository.save(wallet);

        // Create withdraw request
        WithdrawRequest withdrawRequest = new WithdrawRequest();
        withdrawRequest.setUserId(userId);
        withdrawRequest.setAmount(request.getAmount());
        withdrawRequest.setBankAccount(request.getBankAccount());
        withdrawRequest.setBankName(request.getBankName());
        withdrawRequest.setAccountHolderName(request.getAccountHolderName());
        withdrawRequest.setStatus(Status.PENDING);
        withdrawRequestRepository.save(withdrawRequest);

        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setWallet(wallet);
        transaction.setAmount(request.getAmount());
        transaction.setDescription("Withdraw to " + request.getBankName());
        transaction.setType(TransactionType.WITHDRAW);
        transactionRepository.save(transaction);

        log.info("Withdraw request created for user: {}, amount: {}", userId, request.getAmount());
    }

    @Override
    public Page<Transaction> getTransactionHistory(String userId, Pageable pageable) {
        return transactionRepository.findByWallet_UserId(userId, pageable);
    }
}