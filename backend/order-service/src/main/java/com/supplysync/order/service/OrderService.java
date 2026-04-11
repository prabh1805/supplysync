package com.supplysync.order.service;

import com.supplysync.order.dto.*;
import com.supplysync.order.entity.Order;
import com.supplysync.order.entity.OrderItem;
import com.supplysync.order.entity.OrderStatus;
import com.supplysync.order.entity.OrderType;
import com.supplysync.order.exception.InvalidOrderStateException;
import com.supplysync.order.exception.InvalidRequestException;
import com.supplysync.order.exception.OrderNotFoundException;
import com.supplysync.order.repository.OrderRepository;
import com.supplysync.order.service.state.OrderState;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new InvalidRequestException("Order must have at least one item");
        }

        OrderType type = OrderType.valueOf(request.getType().toUpperCase());

        Order order = Order.builder()
                .type(type)
                .supplierId(request.getSupplierId() != null ? UUID.fromString(request.getSupplierId()) : null)
                .notes(request.getNotes())
                .build();

        // build items and calculate total
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemReq : request.getItems()) {
            BigDecimal unitPrice = new BigDecimal(itemReq.getUnitPrice());
            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .productId(UUID.fromString(itemReq.getProductId()))
                    .quantity(itemReq.getQuantity())
                    .unitPrice(unitPrice)
                    .totalPrice(itemTotal)
                    .build();

            order.getItems().add(item);
            total = total.add(itemTotal);
        }
        order.setTotalAmount(total);

        orderRepository.save(order);
        return mapToResponse(order);
    }

    public OrderResponse getOrderById(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order with id '" + id + "' not found"));
        return mapToResponse(order);
    }

    public Page<OrderResponse> getAllOrders(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        return orderRepository.findAll(PageRequest.of(page, size, sort))
                .map(this::mapToResponse);
    }

    @Transactional
    public OrderResponse updateOrderStatus(UUID orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order with id '" + orderId + "' not found"));

        OrderStatus targetStatus = OrderStatus.valueOf(newStatus.toUpperCase());
        OrderState currentState = OrderState.from(order.getStatus());

        // state pattern: check if transition is allowed
        if (!currentState.canTransitionTo(targetStatus)) {
            throw new InvalidOrderStateException(
                    "Cannot transition from " + order.getStatus() + " to " + targetStatus
            );
        }

        order.setStatus(targetStatus);
        orderRepository.save(order);
        return mapToResponse(order);
    }

    public List<OrderResponse> getOrdersByStatus(String status) {
        OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
        return orderRepository.findByStatus(orderStatus)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId().toString())
                        .productId(item.getProductId().toString())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice().toString())
                        .totalPrice(item.getTotalPrice())
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId().toString())
                .type(order.getType().name())
                .supplierId(order.getSupplierId() != null ? order.getSupplierId().toString() : null)
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .notes(order.getNotes())
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .build();
    }
}
