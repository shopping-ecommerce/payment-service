package iuh.fit.se.service.impl;

import iuh.fit.se.config.VNPayConfig;
import iuh.fit.se.service.VNPayService;
import iuh.fit.se.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class VNPayServiceImpl implements VNPayService {
    VNPayConfig vnPayConfig;
    @Override
    public String createPaymentURL(HttpServletRequest request) {
        long amount = Long.parseLong(request.getParameter("amount")) * 100; // multiply by 100 to convert to smallest currency unit
        String bankCode = request.getParameter("bankCode");
        String userId = request.getParameter("userId"); // Get userId from request
        String txnRef = request.getParameter("orderId") +":"+VNPayUtil.getRandomNumber(4); // Unique transaction reference
        String orderInfo = "Thanh toan don hang:" + userId + ":" + txnRef;

        Map<String, String> vnpParamsMap = vnPayConfig.getVNPayConfig();
        vnpParamsMap.put("vnp_TxnRef", userId + "-" + txnRef); // Format: userId-randomNumber
        vnpParamsMap.put("vnp_OrderInfo", orderInfo);
        vnpParamsMap.put("vnp_Amount", String.valueOf(amount));
        if (bankCode != null && !bankCode.isEmpty()) {
            vnpParamsMap.put("vnp_BankCode", bankCode);
        }
        vnpParamsMap.put("vnp_IpAddr", VNPayUtil.getIpAddress(request));
        //build query url
        String queryUrl = VNPayUtil.getPaymentURL(vnpParamsMap, true);
        String hashData = VNPayUtil.getPaymentURL(vnpParamsMap, false);
        String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashData);
        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
        String paymentUrl = vnPayConfig.getVnp_PayUrl() + "?" + queryUrl;
        return paymentUrl;
    }
}
