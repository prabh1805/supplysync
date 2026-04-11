package com.supplysync.order.repository;

import com.supplysync.order.entity.Order;
import com.supplysync.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByStatus(OrderStatus status);
    List<Order> findBySupplierId(UUID supplierId);
}
