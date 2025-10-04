package iuh.fit.se.service;

import iuh.fit.se.entity.WithdrawRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WithdrawRequestService {
    Page<WithdrawRequest> getAllRequests(String status, Pageable pageable);
    Page<WithdrawRequest> getUserRequests(String userId, Pageable pageable);
    void approveRequest(String id);
    void rejectRequest(String id, String reason);
    WithdrawRequest getRequestById(String id);
}