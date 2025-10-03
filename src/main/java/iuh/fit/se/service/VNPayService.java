package iuh.fit.se.service;

import jakarta.servlet.http.HttpServletRequest;

public interface VNPayService {
    String createPaymentURL(HttpServletRequest request);
}
