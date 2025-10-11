package iuh.fit.se.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class DepositRequest {
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "10000", message = "Minimum deposit is 10,000 VND")
    private BigDecimal amount;

    private String description;
}