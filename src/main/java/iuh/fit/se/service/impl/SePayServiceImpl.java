package iuh.fit.se.service.impl;

import iuh.fit.se.dto.request.SePayCreatePaymentRequest;
import iuh.fit.se.dto.response.SePayCheckoutResponse;
import iuh.fit.se.service.SePayService;
import iuh.fit.se.util.SePayUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class SePayServiceImpl implements SePayService {

    @Value("${sepay.merchant-id}")
    private String merchantId;

    @Value("${sepay.secret-key}")
    private String secretKey;

    @Value("${sepay.env:sandbox}")
    private String env;

    @Override
    public SePayCheckoutResponse createCheckoutFormFields(SePayCreatePaymentRequest request) {
        String checkoutUrl = getCheckoutUrl();

        String orderCode = request.getOrderCode();
        long amount = request.getAmount();
        String userId = request.getUserId(); // sau này có thể lấy từ token

        // Dùng LinkedHashMap để giữ nguyên thứ tự khi sign
        Map<String, String> fields = new LinkedHashMap<>();

        // THỨ TỰ THEO FORM MẪU CỦA SEPAY
        fields.put("merchant", merchantId);
        fields.put("currency", "VND");
        fields.put("order_amount", String.valueOf(amount));              // <-- lấy từ request
        fields.put("operation", "PURCHASE");
        fields.put("order_description", "Thanh toán đơn hàng #" + orderCode);
        fields.put("order_invoice_number", orderCode);                   // <-- lấy từ request
        fields.put("customer_id", userId != null ? userId : "UNKNOWN");  // tạm fallback
        fields.put("success_url", "https://shoppingiuh.id.vn/order-success");
        fields.put("error_url", "https://shoppingiuh.id.vn/checkout");
        fields.put("cancel_url", "https://shoppingiuh.id.vn/checkout");

        String signature = SePayUtil.signFields(fields, secretKey);
        fields.put("signature", signature);

        return new SePayCheckoutResponse(checkoutUrl, fields);
    }

    private String getCheckoutUrl() {
        if ("production".equalsIgnoreCase(env)) {
            return "https://pay.sepay.vn/v1/checkout/init";
        } else {
            return "https://pay-sandbox.sepay.vn/v1/checkout/init";
        }
    }
}
