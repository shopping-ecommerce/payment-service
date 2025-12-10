package iuh.fit.se.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SePayCreatePaymentRequest {

    /**
     * Mã đơn hàng / mã session thanh toán.
     * Ví dụ: "DH20251209001" hoặc "SESS_..." nếu dùng PaymentSession.
     */
    @NotBlank(message = "Order code must not be blank")
    private String orderCode;

    /**
     * Số tiền thanh toán (đơn vị VND), ví dụ: 100000.
     */
    @Min(value = 1, message = "Amount must be greater than 0")
    private long amount;

    /**
     * Id người dùng thực hiện thanh toán.
     * Có thể truyền từ FE, hoặc bỏ trống để BE lấy từ token.
     */
    @NotBlank(message = "User ID must not be blank")
    private String userId;
}
