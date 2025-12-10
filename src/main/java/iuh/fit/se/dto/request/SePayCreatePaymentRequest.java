package iuh.fit.se.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SePayCreatePaymentRequest {

    /**
     * Mã đơn hàng / mã session thanh toán.
     * Ví dụ: "DH20251209001" hoặc "SESS_..." nếu dùng PaymentSession.
     */
    private String orderCode;

    /**
     * Số tiền thanh toán (đơn vị VND), ví dụ: 100000.
     */
    private long amount;

    /**
     * Id người dùng thực hiện thanh toán.
     * Có thể truyền từ FE, hoặc bỏ trống để BE lấy từ token.
     */
    private String userId;
}
