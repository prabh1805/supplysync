package com.supplysync.shipment.service.state;

import com.supplysync.shipment.entity.Status;

import java.util.Set;

public enum ShipmentState {
    CREATED(Set.of(Status.PICKED_UP, Status.CANCELLED)),
    PICKED_UP(Set.of(Status.IN_TRANSIT)),
    IN_TRANSIT(Set.of(Status.DELIVERED)),
    DELIVERED(Set.of()),
    CANCELLED(Set.of());

    private final Set<Status> allowedTransitions;

    ShipmentState(Set<Status> allowedTransitions) {
        this.allowedTransitions = allowedTransitions;
    }

    public boolean canTransitionTo(Status target) {
        return allowedTransitions.contains(target);
    }

    public static ShipmentState from(Status status) {
        return ShipmentState.valueOf(status.name());
    }
}
