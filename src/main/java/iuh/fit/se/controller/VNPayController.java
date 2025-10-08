package iuh.fit.se.controller;

import iuh.fit.se.config.VNPayConfig;
import iuh.fit.se.dto.request.DepositRequest;
import iuh.fit.se.dto.response.ApiResponse;
import iuh.fit.se.service.VNPayService;
import iuh.fit.se.service.WalletService;
import iuh.fit.se.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class VNPayController {
    VNPayService vnPayService;
    VNPayConfig vnPayConfig;
    WalletService walletService;
    @GetMapping("/vn-pay")
    ApiResponse<Object> pay(HttpServletRequest request) throws Exception {
        return ApiResponse.builder()
                .code(200)
                .message("Get payment URL successfully")
                .result(vnPayService.createPaymentURL(request))
                .build();
    }

    @GetMapping("/vn-pay-callback")
    public void payCallbackHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("=== Callback received ===");

        Map<String, String> fields = new HashMap<>();
        for (String param : request.getParameterMap().keySet()) {
            fields.put(param, request.getParameter(param));
            System.out.println(param + " = " + request.getParameter(param));
        }

        String vnpSecureHash = fields.remove("vnp_SecureHash");
        String signValue = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(),
                VNPayUtil.getPaymentURL(fields, false));

        // Lấy thông tin từ VNPay
        String status = request.getParameter("vnp_ResponseCode");
        String txnRef = request.getParameter("vnp_TxnRef");
        String amount = request.getParameter("vnp_Amount");
        String orderInfo = request.getParameter("vnp_OrderInfo");
        String transactionNo = request.getParameter("vnp_TransactionNo");
        String bankCode = request.getParameter("vnp_BankCode");

        // ✅ Validate chữ ký
        if (!signValue.equals(vnpSecureHash)) {
            // ❌ Chữ ký không hợp lệ
            String redirectUrl = String.format(
                    "http://localhost:3000/payment/result?status=error&message=%s",
                    URLEncoder.encode("Chữ ký không hợp lệ", StandardCharsets.UTF_8)
            );
            response.sendRedirect(redirectUrl);
            return;
        }

        if ("00".equals(status)) {
            // ✅ Thanh toán thành công
            String userId = extractUserIdFromOrderInfo(orderInfo);

            // TODO: Cập nhật trạng thái đơn hàng vào DB ở đây
            BigDecimal depositAmount = new BigDecimal(amount).divide(new BigDecimal("100"), RoundingMode.DOWN); // Convert to main unit
            BigDecimal commissionRate = new BigDecimal("0.08");
            BigDecimal commission = depositAmount.multiply(commissionRate).setScale(0, RoundingMode.DOWN);  // 8000
            BigDecimal realDepositAmount = depositAmount.subtract(commission);  // 92000// Convert from smallest unit
            DepositRequest depositRequest = new DepositRequest();
            depositRequest.setAmount(realDepositAmount);
            depositRequest.setDescription("Nạp tiền qua VNPay - " + txnRef);

            walletService.deposit(userId, depositRequest);
//            String redirectUrl = String.format(
//                    "http://localhost:3000/payment/result?status=success&txnRef=%s&amount=%s&orderInfo=%s&transactionNo=%s&bankCode=%s",
//                    txnRef,
//                    Long.parseLong(amount) / 100, // Convert về VND
//                    URLEncoder.encode(orderInfo, StandardCharsets.UTF_8),
//                    transactionNo,
//                    bankCode
//            );
            String redirectUrl = "http://localhost:5173/order-success";
            response.sendRedirect(redirectUrl);
        } else {
            // ❌ Thanh toán thất bại
//            String redirectUrl = String.format(
//                    "http://localhost:3000/payment/result?status=failed&code=%s&message=%s&txnRef=%s",
//                    status,
//                    URLEncoder.encode(getErrorMessage(status), StandardCharsets.UTF_8),
//                    txnRef
//            );
            String redirectUrl = "http://localhost:5173/order-failed";
            response.sendRedirect(redirectUrl);
        }
    }

    private String getErrorMessage(String code) {
        return switch (code) {
            case "07" -> "Giao dịch bị nghi ngờ gian lận";
            case "09" -> "Thẻ chưa đăng ký dịch vụ Internet Banking";
            case "10" -> "Xác thực thông tin thẻ không đúng quá 3 lần";
            case "11" -> "Đã hết hạn chờ thanh toán";
            case "12" -> "Thẻ bị khóa";
            case "13" -> "Sai mật khẩu xác thực giao dịch (OTP)";
            case "24" -> "Khách hàng hủy giao dịch";
            case "51" -> "Tài khoản không đủ số dư";
            case "65" -> "Tài khoản đã vượt quá hạn mức giao dịch trong ngày";
            case "75" -> "Ngân hàng thanh toán đang bảo trì";
            case "79" -> "Giao dịch vượt quá số lần nhập sai mật khẩu";
            default -> "Giao dịch thất bại";
        };
    }

    @PostMapping("/vnpay-ipn")
    public ResponseEntity<Map<String, String>> handleIPN(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        for (String param : request.getParameterMap().keySet()) {
            fields.put(param, request.getParameter(param));
        }

        String vnpSecureHash = fields.remove("vnp_SecureHash");
        String signValue = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(),
                VNPayUtil.getPaymentURL(fields, false));

        Map<String, String> response = new HashMap<>();
        if (!signValue.equals(vnpSecureHash)) {
            response.put("RspCode", "97");
            response.put("Message", "Invalid signature");
            return ResponseEntity.ok(response);
        }

        // TODO: Cập nhật DB, kiểm tra trùng lặp giao dịch

        response.put("RspCode", "00");
        response.put("Message", "Confirm Success");
        return ResponseEntity.ok(response);
    }

    private String extractUserIdFromOrderInfo(String orderInfo) {
        // Assuming format: "Thanh toan don hang:userId:randomNumber"
        // or you can pass userId in vnp_TxnRef format: userId-timestamp
        String[] parts = orderInfo.split(":");
        if (parts.length >= 2) {
            return parts[1];
        }
        throw new IllegalArgumentException("Invalid order info format");
    }
}
