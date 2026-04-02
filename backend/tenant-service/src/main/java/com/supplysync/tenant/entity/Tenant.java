package com.supplysync.tenant.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="tenants")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    //Company name;
    @Column(nullable = false,  unique = true)
    private String name;

    @Column(nullable = false,   unique = true)
    private String subdomain;

    private String dbSchema;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,   length = 20)
    @Builder.Default
    private Plan plan = Plan.FREE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,   length = 40)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @CreatedDate
    @Column(nullable = false,   updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

}
