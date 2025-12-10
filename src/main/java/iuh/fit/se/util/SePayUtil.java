package iuh.fit.se.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Base64;

public class SePayUtil {

    // Các field được phép ký (không quyết định thứ tự)
    private static final Set<String> SIGNED_FIELD_NAMES = new LinkedHashSet<>(Arrays.asList(
            "merchant",
            "operation",
            "payment_method",
            "order_amount",
            "currency",
            "order_invoice_number",
            "order_description",
            "customer_id",
            "success_url",
            "error_url",
            "cancel_url"
    ));

    public static String signFields(Map<String, String> fields, String secretKey) {
        List<String> parts = new ArrayList<>();

        // GIỐNG PHP: đi theo thứ tự key của map, rồi lọc theo danh sách cho phép
        for (String field : fields.keySet()) {
            if (!SIGNED_FIELD_NAMES.contains(field)) continue;
            String value = Optional.ofNullable(fields.get(field)).orElse("");
            parts.add(field + "=" + value);
        }

        String signingString = String.join(",", parts);
        System.out.println("[SePay] Signing string = " + signingString);

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(keySpec);
            byte[] rawHmac = mac.doFinal(signingString.getBytes(StandardCharsets.UTF_8));

            String signature = Base64.getEncoder().encodeToString(rawHmac);
            System.out.println("[SePay] Signature = " + signature);
            return signature;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate SePay signature", e);
        }
    }
}
