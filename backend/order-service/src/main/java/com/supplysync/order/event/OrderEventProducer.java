package com.supplysync.order.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes order events to Kafka.
 * Topic: "order-events"
 * Key: orderId (ensures all events for the same order go to the same partition)
 */
@Component
@RequiredArgsConstructor
public class OrderEventProducer {
    private static final String TOPIC = "order-events";
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public void publish(OrderEvent event) {
        kafkaTemplate.send(TOPIC, event.getOrderId(), event);
    }
}
