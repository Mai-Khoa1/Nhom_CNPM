package com.horseracing.controller;

import com.horseracing.dto.common.ApiResponse;
import com.horseracing.dto.common.PageResponse;
import com.horseracing.dto.common.UpdateRequestStatus;
import com.horseracing.dto.common.UpdateTargetType;
import com.horseracing.dto.updaterequest.UpdateRequestResponseDTO;
import com.horseracing.service.CurrentUserService;
import com.horseracing.service.UpdateRequestService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * UpdateRequestController - Ban tổ chức duyệt/từ chối yêu cầu chủ ngựa sửa thông tin Ngựa/Nài.
 * Base path /update-requests.
 */
@RestController
@RequestMapping("/update-requests")
@RequiredArgsConstructor
@Tag(name = "Yêu cầu cập nhật", description = "API duyệt/từ chối yêu cầu sửa thông tin ngựa/nài đã được duyệt trước đó")
public class UpdateRequestController {

    private final UpdateRequestService updateRequestService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UpdateRequestResponseDTO>>> getAll(
            Pageable pageable,
            @RequestParam(required = false) UpdateRequestStatus status,
            @RequestParam(required = false) UpdateTargetType targetType,
            Authentication authentication) {
        boolean isAdmin = currentUserService.hasRole(authentication, "ADMIN");
        String organizerScopeId = currentUserService.resolveOrganizerId(authentication);
        return ResponseEntity.ok(ApiResponse.success(updateRequestService.getAll(pageable, status, targetType, isAdmin, organizerScopeId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UpdateRequestResponseDTO>> getById(@PathVariable String id, Authentication authentication) {
        boolean isAdmin = currentUserService.hasRole(authentication, "ADMIN");
        String organizerScopeId = currentUserService.resolveOrganizerId(authentication);
        return ResponseEntity.ok(ApiResponse.success(updateRequestService.getById(id, isAdmin, organizerScopeId)));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<UpdateRequestResponseDTO>> approve(@PathVariable String id, Authentication authentication) {
        String staffId = currentUserService.resolveMaTK(authentication);
        boolean isAdmin = currentUserService.hasRole(authentication, "ADMIN");
        String organizerScopeId = currentUserService.resolveOrganizerId(authentication);
        return ResponseEntity.ok(ApiResponse.success(updateRequestService.approve(id, staffId, isAdmin, organizerScopeId), "Đã duyệt yêu cầu cập nhật"));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<UpdateRequestResponseDTO>> reject(
            @PathVariable String id, @RequestBody(required = false) Map<String, String> body, Authentication authentication) {
        String staffId = currentUserService.resolveMaTK(authentication);
        boolean isAdmin = currentUserService.hasRole(authentication, "ADMIN");
        String organizerScopeId = currentUserService.resolveOrganizerId(authentication);
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(ApiResponse.success(updateRequestService.reject(id, reason, staffId, isAdmin, organizerScopeId), "Đã từ chối yêu cầu cập nhật"));
    }
}
