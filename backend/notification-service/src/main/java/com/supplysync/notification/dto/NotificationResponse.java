package com.supplysync.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private String id;
    private String type;
    private String title;
    private String message;
    private String recipientEmail;
    private boolean read;
    private LocalDateTime createdAt;
}
