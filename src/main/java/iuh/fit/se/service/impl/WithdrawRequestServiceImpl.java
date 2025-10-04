package iuh.fit.se.service.impl;

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
import iuh.fit.se.service.WithdrawRequestService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
@Slf4j
public class WithdrawRequestServiceImpl implements WithdrawRequestService {

    WithdrawRequestRepository withdrawRequestRepository;
    WalletRepository walletRepository;
    TransactionRepository transactionRepository;

    @Override
    public Page<WithdrawRequest> getAllRequests(String status, Pageable pageable) {
        if (status != null && !status.isEmpty()) {
            return withdrawRequestRepository.findByStatus(Status.valueOf(status), pageable);
        }
        return withdrawRequestRepository.findAll(pageable);
    }

    @Override
    public Page<WithdrawRequest> getUserRequests(String userId, Pageable pageable) {
        return withdrawRequestRepository.findByUserId(userId, pageable);
    }

    @Override
    @Transactional
    public void approveRequest(String id) {
        WithdrawRequest request = withdrawRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Withdraw request not found"));

        if (!Status.PENDING.equals(request.getStatus())) {
            throw new IllegalStateException("Only pending requests can be approved");
        }

        // Update request status
        request.setStatus(Status.APPROVED);
        withdrawRequestRepository.save(request);
        log.info("Withdraw request approved: {} for user: {}, amount: {}",
                id, request.getUserId(), request.getAmount());
        withdrawRequestRepository.delete(request);
        // In real scenario, you would integrate with bank API here to transfer money
        // For now, just log it
    }

    @Override
    @Transactional
    public void rejectRequest(String id, String reason) {
        WithdrawRequest request = withdrawRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Withdraw request not found"));

        if (!Status.PENDING.equals(request.getStatus())) {
            throw new IllegalStateException("Only pending requests can be rejected");
        }

        // Refund the amount back to wallet
        Wallet wallet = walletRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        walletRepository.save(wallet);

        // Create refund transaction
        Transaction transaction = new Transaction();
        transaction.setWallet(wallet);
        transaction.setAmount(request.getAmount());
        transaction.setDescription("Refund for rejected withdraw request: " + reason);
        transaction.setType(TransactionType.WITHDRAW);
        transaction.setStatus(Status.REJECTED);
        transactionRepository.save(transaction);

        // Update request status
        request.setStatus(Status.REJECTED);
        request.setReason(reason);
        withdrawRequestRepository.save(request);

        log.info("Withdraw request rejected: {} for user: {}, reason: {}",
                id, request.getUserId(), reason);
        withdrawRequestRepository.delete(request);
    }

    @Override
    public WithdrawRequest getRequestById(String id) {
        return withdrawRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Withdraw request not found"));
    }
}