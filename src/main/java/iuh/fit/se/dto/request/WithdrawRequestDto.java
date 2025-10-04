package iuh.fit.se.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawRequestDto {
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "50000", message = "Minimum withdrawal is 50,000 VND")
    private BigDecimal amount;

    @NotBlank(message = "Bank account is required")
    private String bankAccount;

    @NotBlank(message = "Bank name is required")
    private String bankName;

    @NotBlank(message = "Account holder name is required")
    private String accountHolderName;
}