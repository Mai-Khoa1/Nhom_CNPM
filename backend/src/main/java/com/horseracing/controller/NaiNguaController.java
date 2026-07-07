package com.horseracing.controller;

import com.horseracing.dto.common.ApiResponse;
import com.horseracing.dto.common.PageResponse;
import com.horseracing.dto.nainghua.NaiNguaRequestDTO;
import com.horseracing.dto.nainghua.NaiNguaResponseDTO;
import com.horseracing.entity.TepTin;
import com.horseracing.exception.ResourceNotFoundException;
import com.horseracing.service.CurrentUserService;
import com.horseracing.service.NaiNguaService;
import com.horseracing.service.UploadService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * NaiNguaController - quản lý nài ngựa (Jockey). Base path /jockeys khớp frontend.
 */
@RestController
@RequestMapping("/jockeys")
@RequiredArgsConstructor
@Tag(name = "Quản lý Jockey", description = "API thêm, sửa, xóa, duyệt và tìm kiếm nài ngựa")
public class NaiNguaController {

    private final NaiNguaService naiNguaService;
    private final UploadService uploadService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NaiNguaResponseDTO>>> getAllJockeys(
            Pageable pageable,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String ownerId,
            @RequestParam(defaultValue = "false") boolean includeInactive,
            Authentication authentication) {
        String effectiveOwnerId = ownerId;
        if (currentUserService.hasRole(authentication, "HORSE_OWNER")) {
            effectiveOwnerId = currentUserService.resolveMaTK(authentication);
        }
        boolean publicOnly = isPublicViewer(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                naiNguaService.getAllJockeys(pageable, keyword, effectiveOwnerId, publicOnly, includeInactive)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NaiNguaResponseDTO>> getJockeyById(@PathVariable String id, Authentication authentication) {
        String requesterMaTK = currentUserService.resolveMaTK(authentication);
        boolean privileged = currentUserService.isPrivileged(authentication);
        return ResponseEntity.ok(ApiResponse.success(naiNguaService.getJockeyById(id, requesterMaTK, privileged)));
    }

    private boolean isPublicViewer(Authentication authentication) {
        return !currentUserService.hasRole(authentication, "HORSE_OWNER") && !currentUserService.isPrivileged(authentication);
    }

    /**
     * Ảnh đại diện nài - endpoint công khai (nằm dưới /jockeys/** đã permitAll), không cần đăng nhập,
     * dùng trực tiếp trong &lt;img src&gt; ở trang khán giả (khác /upload/{id} yêu cầu Bearer token).
     */
    @GetMapping("/{id}/avatar")
    public ResponseEntity<byte[]> getAvatar(@PathVariable String id) {
        TepTin file = naiNguaService.getJockeyAvatarFile(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ảnh đại diện nài", "maNaiNgua", id));
        byte[] content = uploadService.readFileBytes(file);
        MediaType mediaType = file.getContentType() != null
                ? MediaType.parseMediaType(file.getContentType()) : MediaType.APPLICATION_OCTET_STREAM;
        return ResponseEntity.ok().contentType(mediaType).body(content);
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
}
