package com.horseracing.controller;

import com.horseracing.dto.common.ApiResponse;
import com.horseracing.dto.common.PageResponse;
import com.horseracing.dto.notification.NotificationResponseDTO;
import com.horseracing.service.CurrentUserService;
import com.horseracing.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * NotificationController - thông báo của người dùng hiện tại. Base path /notifications khớp frontend.
 */
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Thông báo", description = "API xem và đánh dấu đã đọc thông báo")
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponseDTO>>> getNotifications(
            Pageable pageable, @RequestParam(required = false) Boolean isRead, Authentication authentication) {
        String maTK = currentUserService.resolveMaTK(authentication);
        return ResponseEntity.ok(ApiResponse.success(notificationService.getNotifications(maTK, isRead, pageable)));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(Authentication authentication) {
        String maTK = currentUserService.resolveMaTK(authentication);
        return ResponseEntity.ok(ApiResponse.success(Map.of("count", notificationService.getUnreadCount(maTK))));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markRead(@PathVariable String id) {
        notificationService.markRead(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã đánh dấu đã đọc"));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllRead(Authentication authentication) {
        String maTK = currentUserService.resolveMaTK(authentication);
        notificationService.markAllRead(maTK);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã đánh dấu tất cả đã đọc"));
    }
}
