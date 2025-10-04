package iuh.fit.se.repository;

import iuh.fit.se.entity.WithdrawRequest;
import iuh.fit.se.entity.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WithdrawRequestRepository extends JpaRepository<WithdrawRequest, String> {

    Page<WithdrawRequest> findByUserId(String userId, Pageable pageable);

    Page<WithdrawRequest> findByStatus(Status status, Pageable pageable);

    List<WithdrawRequest> findByUserIdAndStatus(String userId, Status status);

    long countByUserIdAndStatus(String userId, Status status);
}