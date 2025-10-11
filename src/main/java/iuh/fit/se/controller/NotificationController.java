package iuh.fit.se.controller;

import iuh.fit.event.dto.OrderStatusChangedEvent;
import iuh.fit.se.dto.request.DepositRequest;
import iuh.fit.se.service.WalletService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.RestController;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequiredArgsConstructor
public class NotificationController {
    WalletService walletService;

    @KafkaListener(topics = "order-updated", groupId = "payment-service-group", concurrency = "1")
    public void handleOrderStatusChangedEvent(OrderStatusChangedEvent event) {
        log.info("Nhận được sự kiện order-updated cho orderId: {}", event.getOrderId());
        try {
            walletService.deposit(event.getUserId(), DepositRequest.builder()
                            .amount(event.getSubtotal())
                            .description("Hoàn tiền cho đơn hàng: #" + event.getOrderId())
                    .build());
            log.info("Đã xử lý hoàn tiền cho đơn hàng: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("Lỗi khi hoàn tiền cho đơn hàng {}: {}", event.getOrderId(), e.getMessage());
        }
    }

    @KafkaListener(topics = "user-cancel-order", groupId = "payment-service-group", concurrency = "1")
    public void handleUserCancelChangedEvent(OrderStatusChangedEvent event) {
        log.info("Nhận được sự kiện user-cancel-order cho orderId: {}", event.getOrderId());
        try {
            walletService.deposit(event.getUserId(), DepositRequest.builder()
                    .amount(event.getSubtotal())
                    .description("Hoàn tiền cho đơn hàng: #" + event.getOrderId())
                    .build());
            log.info("Đã xử lý hoàn tiền cho đơn hàng: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("Lỗi khi hoàn tiền cho đơn hàng {}: {}", event.getOrderId(), e.getMessage());
        }
    }
}
