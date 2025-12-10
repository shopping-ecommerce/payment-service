package iuh.fit.se.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class SePayCheckoutResponse {
    private String checkoutUrl;
    private Map<String, String> fields;
}
