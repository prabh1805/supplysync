package com.supplysync.notification.service;

import com.supplysync.notification.dto.NotificationRequest;
import com.supplysync.notification.dto.NotificationResponse;
import com.supplysync.notification.entity.Notification;
import com.supplysync.notification.entity.NotificationType;
import com.supplysync.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationResponse create(NotificationRequest request) {
        Notification notification = Notification.builder()
                .tenantId(request.getTenantId())
                .type(NotificationType.valueOf(request.getType().toUpperCase()))
                .title(request.getTitle())
                .message(request.getMessage())
                .recipientEmail(request.getRecipientEmail())
                .build();

        notificationRepository.save(notification);
        return mapToResponse(notification);
    }

    public List<NotificationResponse> getByTenant(String tenantId) {
        return notificationRepository.findByTenantIdOrderByCreatedAtDesc(tenantId)
                .stream().map(this::mapToResponse).toList();
    }

    public List<NotificationResponse> getUnreadByTenant(String tenantId) {
        return notificationRepository.findByTenantIdAndReadFalseOrderByCreatedAtDesc(tenantId)
                .stream().map(this::mapToResponse).toList();
    }

    public NotificationResponse markAsRead(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
        return mapToResponse(notification);
    }

    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId().toString())
                .type(n.getType().name())
                .title(n.getTitle())
                .message(n.getMessage())
                .recipientEmail(n.getRecipientEmail())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
