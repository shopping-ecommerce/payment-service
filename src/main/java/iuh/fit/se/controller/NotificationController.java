package iuh.fit.se.controller;

import feign.FeignException;
import iuh.fit.event.dto.OrderStatusChangedEvent;
import iuh.fit.event.dto.SellerViolationEvent;
import iuh.fit.se.dto.request.DepositRequest;
import iuh.fit.se.dto.response.VersionResp;
import iuh.fit.se.repository.httpclient.PolicyClient;
import iuh.fit.se.service.WalletService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequiredArgsConstructor
public class NotificationController {
    WalletService walletService;
    PolicyClient policyClient;
    @KafkaListener(topics = "order-updated", groupId = "payment-service-group", concurrency = "1")
    public void handleOrderStatusChangedEvent(OrderStatusChangedEvent event) {
        log.info("Nhận được sự kiện order-updated cho orderId: {}", event.toString());
        try {
            if ("DELIVERED".equals(event.getStatus())) {
                try {
                    ResponseEntity<VersionResp> response = policyClient.getSellerTosEffective(LocalDate.now());
                    walletService.deposit(event.getSellerId(), DepositRequest.builder()
                            .amount(event.getTotalAmount().subtract(
                                    event.getTotalAmount().multiply(response.getBody().commissionPercent().divide(BigDecimal.valueOf(100)))))
                            .description("Hoàn tiền thưởng cho đơn hàng: #" + event.getOrderId())
                            .build());
                    log.info("Đã xử lý hoàn tiền thưởng cho đơn hàng: {}", event.getOrderId());
                }catch (FeignException e){
                    log.info("Lỗi khi gọi PolicyClient: {}", e.getMessage());
                }
            } else if ("CANCELLED".equals(event.getStatus()) && "BANK_TRANSFER".equals(event.getPaymentMethod())) {
                walletService.deposit(event.getUserId(), DepositRequest.builder()
                        .amount(event.getTotalAmount())
                        .description("Hoàn tiền cho đơn hàng: #" + event.getOrderId())
                        .build());
                log.info("Đã xử lý hoàn tiền cho đơn hàng: {}", event.getOrderId());
            }
        } catch (Exception e) {
            log.error("Lỗi khi hoàn tiền cho đơn hàng {}: {}", event.getOrderId(), e.getMessage());
        }
    }

    @KafkaListener(topics = "user-cancel-order", groupId = "payment-service-group", concurrency = "1")
    public void handleUserCancelChangedEvent(OrderStatusChangedEvent event) {
        log.info("Nhận được sự kiện user-cancel-order cho orderId: {}", event.getOrderId());
        try {
            if (!"BANK_TRANSFER".equals(event.getPaymentMethod())) {
                log.info("Trạng thái đơn hàng không phải BANK_TRANSFER, không thực hiện hoàn tiền cho đơn hàng: {}", event.getOrderId());
                return;
            }
            walletService.deposit(event.getUserId(), DepositRequest.builder()
                    .amount(event.getTotalAmount())
                    .description("Hoàn tiền cho đơn hàng: #" + event.getOrderId())
                    .build());
            log.info("Đã xử lý hoàn tiền cho đơn hàng: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("Lỗi khi hoàn tiền cho đơn hàng {}: {}", event.getOrderId(), e.getMessage());
        }
    }

    @KafkaListener(topics = "seller-violations", groupId = "payment-service-group", concurrency = "1")
    public void handleSellerViolations(SellerViolationEvent event) {
        log.info("Nhận được sự kiện seller-violations cho orderId: {}", event.getOrderId());
        if (!"BANK_TRANSFER".equals(event.getMethod())) {
            log.info("Trạng thái đơn hàng không phải BANK_TRANSFER, không thực hiện hoàn tiền cho đơn hàng: {}", event.getOrderId());
            return;
        }
        walletService.deposit(event.getUserId(), DepositRequest.builder()
                .amount(event.getTotalPrice())
                .description("Hoàn tiền cho đơn hàng: #" + event.getOrderId())
                .build());
        log.info("Đã xử lý hoàn tiền cho đơn hàng: {}", event.getOrderId());
    }
}
