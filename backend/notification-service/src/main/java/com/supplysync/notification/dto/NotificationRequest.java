package com.supplysync.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private String tenantId;
    private String type;
    private String title;
    private String message;
    private String recipientEmail;
}
