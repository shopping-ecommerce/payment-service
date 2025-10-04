package iuh.fit.se.controller;
import iuh.fit.se.dto.request.WithdrawRequestDto;
import iuh.fit.se.dto.response.ApiResponse;
import iuh.fit.se.dto.response.TransactionResponse;
import iuh.fit.se.dto.response.WalletResponse;
import iuh.fit.se.entity.Transaction;
import iuh.fit.se.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/wallet")
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class WalletController {

    WalletService walletService;

    @PostMapping("/create")
    public ApiResponse<Object>createWallet(@RequestParam String userId) {
        return ApiResponse.builder()
                .code(200)
                .message("Wallet created successfully")
                .result(walletService.createWallet(userId))
                .build();
    }

    @GetMapping("/{userId}")
    public ApiResponse<WalletResponse> getWallet(@PathVariable String userId) {
        WalletResponse wallet = walletService.getWalletByUserId(userId);
        return ApiResponse.<WalletResponse>builder()
                .code(200)
                .message("Wallet fetched successfully")
                .result(wallet)
                .build();
    }
    @PostMapping("/{userId}/withdraw")
    public ApiResponse<Object> withdraw(
            @PathVariable String userId,
            @Valid @RequestBody WithdrawRequestDto request) {
        walletService.withdraw(userId, request);
        return ApiResponse.builder()
                .code(200)
                .message("Withdraw request submitted successfully")
                .build();
    }

    @GetMapping("/{userId}/transactions")
    public ApiResponse<Page<TransactionResponse>> getTransactions(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Transaction> transactions = walletService.getTransactionHistory(userId, pageable);

        Page<TransactionResponse> response = transactions.map(t -> TransactionResponse.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .description(t.getDescription())
                .type(t.getType().name())
                .createdAt(t.getCreatedAt())
                .build());

        return ApiResponse.<Page<TransactionResponse>>builder()
                .code(200)
                .message("Get transactions successfully")
                .result(response)
                .build();
    }

    @GetMapping("/{userId}/balance")
    public ApiResponse<WalletResponse> getBalance(@PathVariable String userId) {
        WalletResponse wallet = walletService.getWalletByUserId(userId);
        return ApiResponse.<WalletResponse>builder()
                .code(200)
                .message("Get balance successfully")
                .result(wallet)
                .build();
    }
}