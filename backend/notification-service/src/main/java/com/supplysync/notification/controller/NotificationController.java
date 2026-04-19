package com.supplysync.notification.controller;

import com.supplysync.notification.dto.NotificationRequest;
import com.supplysync.notification.dto.NotificationResponse;
import com.supplysync.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<NotificationResponse> create(@RequestBody NotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.create(request));
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<List<NotificationResponse>> getByTenant(@PathVariable String tenantId) {
        return ResponseEntity.ok(notificationService.getByTenant(tenantId));
    }

    @GetMapping("/tenant/{tenantId}/unread")
    public ResponseEntity<List<NotificationResponse>> getUnread(@PathVariable String tenantId) {
        return ResponseEntity.ok(notificationService.getUnreadByTenant(tenantId));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }
}
