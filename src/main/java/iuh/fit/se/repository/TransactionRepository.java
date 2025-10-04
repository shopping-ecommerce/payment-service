package iuh.fit.se.repository;

import iuh.fit.se.entity.Transaction;
import iuh.fit.se.entity.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    Page<Transaction> findByWallet_UserId(String userId, Pageable pageable);

    List<Transaction> findByWallet_UserIdAndType(String userId, TransactionType type);

    @Query("SELECT t FROM Transaction t WHERE t.wallet.userId = :userId AND t.createdAt BETWEEN :startDate AND :endDate")
    List<Transaction> findTransactionsByDateRange(String userId, LocalDateTime startDate, LocalDateTime endDate);

    boolean existsByDescription(String description);
}