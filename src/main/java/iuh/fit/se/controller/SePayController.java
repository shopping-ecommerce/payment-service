// src/main/java/iuh/fit/se/controller/SePayController.java
package iuh.fit.se.controller;

import iuh.fit.se.dto.request.SePayCreatePaymentRequest;
import iuh.fit.se.dto.request.SePayIpnPayload;
import iuh.fit.se.dto.response.ApiResponse;
import iuh.fit.se.dto.response.SePayCheckoutResponse;
import iuh.fit.se.service.SePayService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/sepay")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
@Slf4j
public class SePayController {

    SePayService sePayService;

    @Value("${sepay.secret-key}")
    @NonFinal
    String secretKey;

    // ===== TEST: dữ liệu cứng =====
//    @PostMapping("/test-create")
//    public ApiResponse<SePayCheckoutResponse> createTestPayment() {
//        SePayCheckoutResponse checkout = sePayService.createCheckoutFormFields();
//
//        return ApiResponse.<SePayCheckoutResponse>builder()
//                .code(200)
//                .message("Create SePay checkout successfully")
//                .result(checkout)
//                .build();
//    }

    // ===== CREATE THẬT: FE gửi orderCode + amount =====
    @PostMapping("/create")
    public ApiResponse<SePayCheckoutResponse> createPayment(
            @RequestBody SePayCreatePaymentRequest request
            // @AuthenticationPrincipal CustomUserDetails userDetails  // sau này nếu muốn
    ) {
        String userId = request.getUserId();
        // nếu muốn lấy từ token:
        // if (userId == null) userId = userDetails.getUserId();

        SePayCheckoutResponse checkout = sePayService.createCheckoutFormFields(
            request
        );

        return ApiResponse.<SePayCheckoutResponse>builder()
                .code(200)
                .message("Create SePay checkout successfully")
                .result(checkout)
                .build();
    }

    // ===== IPN =====
    @PostMapping("/ipn")
    public ResponseEntity<?> handleIpn(
            @RequestHeader(name = "X-Secret-Key", required = false) String xSecretKey,
            @RequestBody SePayIpnPayload payload
    ) {
        log.info("=== SePay IPN received ===");
        System.out.println("=== SePay IPN received ===");
        System.out.println("Header X-Secret-Key = " + xSecretKey);
        System.out.println("Payload = " + payload);

        if (xSecretKey == null || !xSecretKey.equals(secretKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "success", false,
                            "message", "Invalid secret key"
                    ));
        }

        if (!"ORDER_PAID".equalsIgnoreCase(payload.getNotification_type())) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Notification ignored"
            ));
        }

        SePayIpnPayload.SePayIpnOrder order = payload.getOrder();
        String invoiceNumber = order.getOrder_invoice_number();
        String amount = order.getOrder_amount();
        String status = order.getOrder_status();

        // TODO: gọi service cập nhật đơn / payment session
        // sePayOrderHandler.handleOrderPaid(invoiceNumber, amount, payload.getTransaction().getTransaction_id());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Processed successfully"
        ));
    }
}
