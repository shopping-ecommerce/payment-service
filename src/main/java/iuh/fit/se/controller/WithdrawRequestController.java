package iuh.fit.se.controller;

import iuh.fit.se.dto.response.ApiResponse;
import iuh.fit.se.entity.WithdrawRequest;
import iuh.fit.se.service.WithdrawRequestService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/withdraw-requests")
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class WithdrawRequestController {

    WithdrawRequestService withdrawRequestService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<WithdrawRequest>>> getAllRequests(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<WithdrawRequest> requests = withdrawRequestService.getAllRequests(status, pageable);

        return ResponseEntity.ok(ApiResponse.<Page<WithdrawRequest>>builder()
                .code(200)
                .message("Get withdraw requests successfully")
                .result(requests)
                .build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<WithdrawRequest>>> getUserRequests(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<WithdrawRequest> requests = withdrawRequestService.getUserRequests(userId, pageable);

        return ResponseEntity.ok(ApiResponse.<Page<WithdrawRequest>>builder()
                .code(200)
                .message("Get user withdraw requests successfully")
                .result(requests)
                .build());
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<Object>> approveRequest(@PathVariable String id) {
        withdrawRequestService.approveRequest(id);
        return ResponseEntity.ok(ApiResponse.builder()
                .code(200)
                .message("Withdraw request approved successfully")
                .build());
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<Object>> rejectRequest(
            @PathVariable String id,
            @RequestParam String reason) {
        withdrawRequestService.rejectRequest(id, reason);
        return ResponseEntity.ok(ApiResponse.builder()
                .code(200)
                .message("Withdraw request rejected successfully")
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WithdrawRequest>> getRequestById(@PathVariable String id) {
        WithdrawRequest request = withdrawRequestService.getRequestById(id);
        return ResponseEntity.ok(ApiResponse.<WithdrawRequest>builder()
                .code(200)
                .message("Get withdraw request successfully")
                .result(request)
                .build());
    }
}