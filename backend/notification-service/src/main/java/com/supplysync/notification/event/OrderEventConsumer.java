package com.supplysync.notification.event;

import com.supplysync.notification.entity.Notification;
import com.supplysync.notification.entity.NotificationType;
import com.supplysync.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Listens to Kafka "order-events" topic.
 * When an order status changes, automatically creates a notification.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {
    private final NotificationRepository notificationRepository;

    @KafkaListener(topics = "order-events", groupId = "notification-service")
    public void handleOrderEvent(OrderEvent event) {
        log.info("Received order event: orderId={}, status={}", event.getOrderId(), event.getStatus());

        NotificationType type = switch (event.getStatus()) {
            case "CONFIRMED" -> NotificationType.ORDER_CONFIRMED;
            case "DRAFT" -> NotificationType.ORDER_PLACED;
            default -> null;
        };

        if (type == null) return; // not all status changes need notifications

        Notification notification = Notification.builder()
                .tenantId(event.getTenantId())
                .type(type)
                .title("Order " + event.getStatus())
                .message("Order " + event.getOrderId() + " has been " + event.getStatus().toLowerCase()
                        + ". Total: $" + event.getTotalAmount())
                .build();

        notificationRepository.save(notification);
        log.info("Notification created for order {}", event.getOrderId());
    }
}
