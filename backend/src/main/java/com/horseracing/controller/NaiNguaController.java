package com.horseracing.controller;

import com.horseracing.dto.common.ApiResponse;
import com.horseracing.dto.common.JockeyStatus;
import com.horseracing.dto.common.PageResponse;
import com.horseracing.dto.nainghua.NaiNguaRequestDTO;
import com.horseracing.dto.nainghua.NaiNguaResponseDTO;
import com.horseracing.service.CurrentUserService;
import com.horseracing.service.NaiNguaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * NaiNguaController - quản lý nài ngựa (Jockey). Base path /jockeys khớp frontend.
 */
@RestController
@RequestMapping("/jockeys")
@RequiredArgsConstructor
@Tag(name = "Quản lý Jockey", description = "API thêm, sửa, xóa, duyệt và tìm kiếm nài ngựa")
public class NaiNguaController {

    private final NaiNguaService naiNguaService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NaiNguaResponseDTO>>> getAllJockeys(
            Pageable pageable,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) JockeyStatus status,
            @RequestParam(required = false) String ownerId,
            Authentication authentication) {
        String effectiveOwnerId = ownerId;
        JockeyStatus effectiveStatus = status;
        if (currentUserService.hasRole(authentication, "HORSE_OWNER")) {
            effectiveOwnerId = currentUserService.resolveMaTK(authentication);
        } else if (!currentUserService.isPrivileged(authentication)) {
            effectiveStatus = JockeyStatus.APPROVED;
        }
        return ResponseEntity.ok(ApiResponse.success(
                naiNguaService.getAllJockeys(pageable, keyword, effectiveStatus, effectiveOwnerId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NaiNguaResponseDTO>> getJockeyById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(naiNguaService.getJockeyById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NaiNguaResponseDTO>> createJockey(
            @Valid @RequestBody NaiNguaRequestDTO dto, Authentication authentication) {
        String maTK = currentUserService.resolveMaTK(authentication);
        NaiNguaResponseDTO created = naiNguaService.createJockey(dto, maTK, maTK);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created, "Thêm jockey thành công"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NaiNguaResponseDTO>> updateJockey(
            @PathVariable String id, @Valid @RequestBody NaiNguaRequestDTO dto, Authentication authentication) {
        String staffId = currentUserService.resolveMaTK(authentication);
        boolean privileged = currentUserService.isPrivileged(authentication);
        return ResponseEntity.ok(ApiResponse.success(naiNguaService.updateJockey(id, dto, staffId, privileged)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteJockey(@PathVariable String id, Authentication authentication) {
        String staffId = currentUserService.resolveMaTK(authentication);
        boolean privileged = currentUserService.isPrivileged(authentication);
        naiNguaService.deleteJockey(id, staffId, privileged);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa jockey thành công"));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<NaiNguaResponseDTO>> approveJockey(@PathVariable String id, Authentication authentication) {
        String staffId = currentUserService.resolveMaTK(authentication);
        return ResponseEntity.ok(ApiResponse.success(naiNguaService.approveJockey(id, staffId), "Đã duyệt jockey"));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<NaiNguaResponseDTO>> rejectJockey(
            @PathVariable String id, @RequestBody(required = false) Map<String, String> body, Authentication authentication) {
        String staffId = currentUserService.resolveMaTK(authentication);
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(ApiResponse.success(naiNguaService.rejectJockey(id, reason, staffId), "Đã từ chối jockey"));
    }
}
