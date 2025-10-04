package iuh.fit.se.repository;

import iuh.fit.se.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, String> {

//    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Wallet> findByUserId(String userId);

    boolean existsByUserId(String userId);
}