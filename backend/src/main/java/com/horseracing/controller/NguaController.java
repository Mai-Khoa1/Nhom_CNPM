package com.horseracing.controller;

import com.horseracing.dto.common.ApiResponse;
import com.horseracing.dto.common.HorseStatus;
import com.horseracing.dto.common.PageResponse;
import com.horseracing.dto.ngua.NguaRequestDTO;
import com.horseracing.dto.ngua.NguaResponseDTO;
import com.horseracing.dto.result.RaceHistoryItemDTO;
import com.horseracing.service.CurrentUserService;
import com.horseracing.service.NguaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * NguaController - quản lý ngựa đua (bảng Ngua). Base path /horses khớp frontend.
 */
@RestController
@RequestMapping("/horses")
@RequiredArgsConstructor
@Tag(name = "Quản lý Ngựa", description = "API thêm, sửa, xóa, duyệt và tìm kiếm ngựa đua")
public class NguaController {

    private final NguaService nguaService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NguaResponseDTO>>> getAllHorses(
            Pageable pageable,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) HorseStatus status,
            @RequestParam(required = false) String ownerId) {
        return ResponseEntity.ok(ApiResponse.success(nguaService.getAllHorses(pageable, keyword, status, ownerId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NguaResponseDTO>> getHorseById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(nguaService.getHorseById(id)));
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
        return ResponseEntity.ok(ApiResponse.success(nguaService.updateHorse(id, dto, staffId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteHorse(@PathVariable String id, Authentication authentication) {
        String staffId = currentUserService.resolveMaTK(authentication);
        nguaService.deleteHorse(id, staffId);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa ngựa thành công"));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<NguaResponseDTO>> approveHorse(@PathVariable String id, Authentication authentication) {
        String staffId = currentUserService.resolveMaTK(authentication);
        return ResponseEntity.ok(ApiResponse.success(nguaService.approveHorse(id, staffId), "Đã duyệt ngựa"));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<NguaResponseDTO>> rejectHorse(
            @PathVariable String id, @RequestBody(required = false) Map<String, String> body, Authentication authentication) {
        String staffId = currentUserService.resolveMaTK(authentication);
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(ApiResponse.success(nguaService.rejectHorse(id, reason, staffId), "Đã từ chối ngựa"));
    }

    @PatchMapping("/{id}/disqualify")
    public ResponseEntity<ApiResponse<NguaResponseDTO>> disqualifyHorse(
            @PathVariable String id, @RequestBody(required = false) Map<String, String> body, Authentication authentication) {
        String staffId = currentUserService.resolveMaTK(authentication);
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(ApiResponse.success(nguaService.disqualifyHorse(id, reason, staffId), "Đã loại ngựa"));
    }
}
