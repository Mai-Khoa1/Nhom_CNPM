package com.horseracing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String maTK;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String type;

    private Boolean isRead = false;

    private LocalDateTime createdAt = LocalDateTime.now();
}