package iuh.fit.se.entity;

import iuh.fit.se.entity.enums.Status;
import iuh.fit.se.entity.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    @ManyToOne
    @JoinColumn(name="wallet_id", nullable=false)
    Wallet wallet;
    @Column(name = "amount", precision = 15, scale = 2)
    BigDecimal amount;
    @Column(length = 255)
    String description;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    TransactionType type;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    Status status;
    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
