package iuh.fit.se.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class SePayIpnPayload {
    Long timestamp;
    String notification_type;
    SePayIpnOrder order;
    SePayIpnTransaction transaction;
    SePayIpnCustomer customer;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = lombok.AccessLevel.PRIVATE)
    public static class SePayIpnOrder {
        String order_status;
        String order_amount;
        String order_currency;
        String order_invoice_number;
        String order_description;
        String custom_data;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = lombok.AccessLevel.PRIVATE)
    public static class SePayIpnTransaction {
        String transaction_id;
        String transaction_status;
        String transaction_amount;
        String transaction_currency;
        String transaction_method;
        String transaction_reference;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = lombok.AccessLevel.PRIVATE)
    public static class SePayIpnCustomer {
        String customer_id;
        String customer_name;
        String customer_email;
        String customer_phone;
    }
}
