package com.supplysync.order.service.state;

import com.supplysync.order.entity.OrderStatus;

import java.util.Set;

/**
 * State pattern: each status knows which statuses it can transition to.
 * Instead of a big if/else chain, each enum value defines its own allowed transitions.
 */
public enum OrderState {

    DRAFT(Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED)),
    CONFIRMED(Set.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED)),
    PROCESSING(Set.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED)),
    SHIPPED(Set.of(OrderStatus.DELIVERED)),
    DELIVERED(Set.of(OrderStatus.COMPLETED)),
    COMPLETED(Set.of()),    // terminal
    CANCELLED(Set.of());    // terminal

    private final Set<OrderStatus> allowedTransitions;

    OrderState(Set<OrderStatus> allowedTransitions) {
        this.allowedTransitions = allowedTransitions;
    }

    public boolean canTransitionTo(OrderStatus target) {
        return allowedTransitions.contains(target);
    }

    public static OrderState from(OrderStatus status) {
        return OrderState.valueOf(status.name());
    }
}
