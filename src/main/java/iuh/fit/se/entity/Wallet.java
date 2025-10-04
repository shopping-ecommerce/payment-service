package iuh.fit.se.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    @Column(name = "user_id", unique = true)
    String userId;
    @Column(name = "balance", precision = 19, scale = 2)
    BigDecimal balance;
    LocalDateTime modifiedAt;
    @PrePersist
    void preInsert() {
        if (this.balance == null) {
            this.balance = BigDecimal.ZERO;
        }
        this.modifiedAt = LocalDateTime.now();
    }
    @PreUpdate
    void preUpdate() {
        this.modifiedAt = LocalDateTime.now();
    }
}
