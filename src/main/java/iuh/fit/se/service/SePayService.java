package iuh.fit.se.service;

import iuh.fit.se.dto.request.SePayCreatePaymentRequest;
import iuh.fit.se.dto.response.SePayCheckoutResponse;

public interface SePayService {
    SePayCheckoutResponse createCheckoutFormFields(SePayCreatePaymentRequest request);
}
