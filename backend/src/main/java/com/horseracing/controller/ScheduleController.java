package com.horseracing.controller;

import com.horseracing.dto.common.ApiResponse;
import com.horseracing.dto.common.PageResponse;
import com.horseracing.dto.common.RaceStatus;
import com.horseracing.dto.lane.LaneResponseDTO;
import com.horseracing.dto.registration.RegistrationResponseDTO;
import com.horseracing.dto.schedule.ScheduleRequestDTO;
import com.horseracing.dto.schedule.ScheduleResponseDTO;
import com.horseracing.service.CurrentUserService;
import com.horseracing.service.RegistrationService;
import com.horseracing.service.ScheduleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ScheduleController - quản lý chặng đua (bảng ChangDua). Base path /races khớp frontend.
 */
@RestController
@RequestMapping("/races")
@RequiredArgsConstructor
@Tag(name = "Quản lý Chặng đua", description = "API thêm, sửa, xóa, công bố chặng đua")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final RegistrationService registrationService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ScheduleResponseDTO>>> getAllRaces(
            Pageable pageable,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) RaceStatus status,
            @RequestParam(required = false) String seasonId,
            @RequestParam(required = false) String organizerId,
            Authentication authentication) {
        // ORGANIZER luôn chỉ thấy race của chính BTC mình (bỏ qua organizerId client gửi lên nếu có);
        // các role khác (Chủ ngựa chọn BTC khi đăng ký thi đấu, khách xem công khai...) được lọc theo
        // organizerId nếu có chỉ định, không thì thấy tất cả.
        String organizerScopeId = currentUserService.resolveOrganizerId(authentication);
        String effectiveOrganizerId = organizerScopeId != null ? organizerScopeId : organizerId;
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getAllRaces(pageable, keyword, status, seasonId, effectiveOrganizerId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ScheduleResponseDTO>> getRaceById(@PathVariable String id, Authentication authentication) {
        String organizerScopeId = currentUserService.resolveOrganizerId(authentication);
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getRaceById(id, organizerScopeId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ScheduleResponseDTO>> createRace(
            @Valid @RequestBody ScheduleRequestDTO dto, Authentication authentication) {
        String staffId = currentUserService.resolveMaTK(authentication);
        String organizerScopeId = currentUserService.resolveOrganizerId(authentication);
        ScheduleResponseDTO created = scheduleService.createRace(dto, staffId, organizerScopeId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created, "Tạo chặng đua thành công"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ScheduleResponseDTO>> updateRace(
            @PathVariable String id, @Valid @RequestBody ScheduleRequestDTO dto, Authentication authentication) {
        String staffId = currentUserService.resolveMaTK(authentication);
        String organizerScopeId = currentUserService.resolveOrganizerId(authentication);
        return ResponseEntity.ok(ApiResponse.success(scheduleService.updateRace(id, dto, staffId, organizerScopeId), "Cập nhật chặng đua thành công"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRace(@PathVariable String id, Authentication authentication) {
        String staffId = currentUserService.resolveMaTK(authentication);
        String organizerScopeId = currentUserService.resolveOrganizerId(authentication);
        scheduleService.deleteRace(id, staffId, organizerScopeId);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa chặng đua thành công"));
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<ScheduleResponseDTO>> publishRace(
            @PathVariable String id, Authentication authentication) {
        String staffId = currentUserService.resolveMaTK(authentication);
        String organizerScopeId = currentUserService.resolveOrganizerId(authentication);
        return ResponseEntity.ok(ApiResponse.success(scheduleService.publishRace(id, staffId, organizerScopeId), "Đã công bố chặng đua"));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<ScheduleResponseDTO>> cancelRace(
            @PathVariable String id, @RequestBody(required = false) java.util.Map<String, String> body, Authentication authentication) {
        String staffId = currentUserService.resolveMaTK(authentication);
        String organizerScopeId = currentUserService.resolveOrganizerId(authentication);
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(ApiResponse.success(scheduleService.cancelRace(id, reason, staffId, organizerScopeId), "Đã hủy chặng đua"));
    }

    @GetMapping("/{id}/registrations")
    public ResponseEntity<ApiResponse<PageResponse<RegistrationResponseDTO>>> getRegistrations(
            @PathVariable String id, Pageable pageable, Authentication authentication) {
        String organizerScopeId = currentUserService.resolveOrganizerId(authentication);
        return ResponseEntity.ok(ApiResponse.success(registrationService.getRegistrationsByRace(id, pageable, organizerScopeId)));
    }

    @GetMapping("/{id}/lanes")
    public ResponseEntity<ApiResponse<List<LaneResponseDTO>>> getLanes(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(registrationService.getLanesByRace(id)));
    }
}
