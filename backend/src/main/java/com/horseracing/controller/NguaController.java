package com.horseracing.controller;

import com.horseracing.dto.common.ApiResponse;
import com.horseracing.dto.common.PageResponse;
import com.horseracing.dto.ngua.NguaRequestDTO;
import com.horseracing.dto.ngua.NguaResponseDTO;
import com.horseracing.dto.result.RaceHistoryItemDTO;
import com.horseracing.entity.TepTin;
import com.horseracing.exception.ResourceNotFoundException;
import com.horseracing.service.CurrentUserService;
import com.horseracing.service.NguaService;
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

import java.util.Collections;
import java.util.List;

/**
 * NguaController - quản lý ngựa đua (bảng Ngua). Base path /horses khớp frontend.
 */
@RestController
@RequestMapping("/horses")
@RequiredArgsConstructor
@Tag(name = "Quản lý Ngựa", description = "API thêm, sửa, xóa, duyệt và tìm kiếm ngựa đua")
public class NguaController {

    private final NguaService nguaService;
    private final UploadService uploadService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NguaResponseDTO>>> getAllHorses(
            Pageable pageable,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String ownerId,
            @RequestParam(defaultValue = "false") boolean includeInactive,
            Authentication authentication) {
        String effectiveOwnerId = ownerId;
        if (currentUserService.hasRole(authentication, "HORSE_OWNER")) {
            // Chủ ngựa chỉ được xem ngựa của chính mình, bỏ qua ownerId client gửi lên.
            effectiveOwnerId = currentUserService.resolveMaTK(authentication);
        }
        // Mục 4.2: khách/SPECTATOR (không phải chủ ngựa, không phải ADMIN/ORGANIZER) chỉ thấy ngựa đã
        // có ít nhất 1 đăng ký thi đấu được duyệt - chưa đăng ký ở đâu thì không hiển thị ra ngoài.
        boolean publicOnly = isPublicViewer(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                nguaService.getAllHorses(pageable, keyword, effectiveOwnerId, publicOnly, includeInactive)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NguaResponseDTO>> getHorseById(@PathVariable String id, Authentication authentication) {
        String requesterMaTK = currentUserService.resolveMaTK(authentication);
        boolean privileged = currentUserService.isPrivileged(authentication);
        return ResponseEntity.ok(ApiResponse.success(nguaService.getHorseById(id, requesterMaTK, privileged)));
    }

    private boolean isPublicViewer(Authentication authentication) {
        return !currentUserService.hasRole(authentication, "HORSE_OWNER") && !currentUserService.isPrivileged(authentication);
    }

    /**
     * Ảnh đại diện ngựa - endpoint công khai (nằm dưới /horses/** đã permitAll), không cần đăng nhập,
     * dùng trực tiếp trong &lt;img src&gt; ở trang khán giả (khác /upload/{id} yêu cầu Bearer token).
     */
    @GetMapping("/{id}/avatar")
    public ResponseEntity<byte[]> getAvatar(@PathVariable String id) {
        TepTin file = nguaService.getHorseAvatarFile(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ảnh đại diện ngựa", "maNgua", id));
        byte[] content = uploadService.readFileBytes(file);
        MediaType mediaType = file.getContentType() != null
                ? MediaType.parseMediaType(file.getContentType()) : MediaType.APPLICATION_OCTET_STREAM;
        return ResponseEntity.ok().contentType(mediaType).body(content);
    }

    @GetMapping("/{id}/race-history")
    public ResponseEntity<ApiResponse<List<RaceHistoryItemDTO>>> getRaceHistory(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(nguaService.getRaceHistory(id)));
    }

    @GetMapping("/{id}/health")
    public ResponseEntity<ApiResponse<List<Object>>> getHealthRecords(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(Collections.emptyList()));
    }

    @GetMapping("/{id}/doping")
    public ResponseEntity<ApiResponse<List<Object>>> getDopingRecords(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(Collections.emptyList()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NguaResponseDTO>> createHorse(
            @Valid @RequestBody NguaRequestDTO dto, Authentication authentication) {
        String maTK = currentUserService.resolveMaTK(authentication);
        NguaResponseDTO created = nguaService.createHorse(dto, maTK, maTK);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created, "Thêm ngựa thành công"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NguaResponseDTO>> updateHorse(
            @PathVariable String id, @Valid @RequestBody NguaRequestDTO dto, Authentication authentication) {
        String staffId = currentUserService.resolveMaTK(authentication);
        boolean privileged = currentUserService.isPrivileged(authentication);
        return ResponseEntity.ok(ApiResponse.success(nguaService.updateHorse(id, dto, staffId, privileged)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteHorse(@PathVariable String id, Authentication authentication) {
        String staffId = currentUserService.resolveMaTK(authentication);
        boolean privileged = currentUserService.isPrivileged(authentication);
        nguaService.deleteHorse(id, staffId, privileged);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa ngựa thành công"));
    }
}
