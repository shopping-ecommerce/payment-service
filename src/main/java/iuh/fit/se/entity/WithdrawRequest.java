package iuh.fit.se.entity;

import iuh.fit.se.entity.enums.Status;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "withdraw_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WithdrawRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String userId;
    @Column(nullable = false, precision = 19, scale = 2)
    BigDecimal amount;
    @Column(nullable = false)
    String bankAccount;
    @Column(nullable = false)
    String bankName;
    @Column(nullable = false)
    String accountHolderName;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    Status status; // PENDING, APPROVED, REJECTED
    String reason; // Optional notes for rejection
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = Status.PENDING;
        }
    }
    @PreUpdate
    protected void onUpdate() {
        if (Status.APPROVED.equals(status)) {
            approvedAt = LocalDateTime.now();
        }
    }
}
