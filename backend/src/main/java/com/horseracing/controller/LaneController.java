package com.horseracing.controller;

import com.horseracing.dto.common.ApiResponse;
import com.horseracing.dto.lane.LaneRequestDTO;
import com.horseracing.dto.lane.LaneResponseDTO;
import com.horseracing.service.CurrentUserService;
import com.horseracing.service.RegistrationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * LaneController - gán làn đua (Lane) cho các đăng ký thi đấu. Base path /lanes khớp frontend.
 */
@RestController
@RequestMapping("/lanes")
@RequiredArgsConstructor
@Tag(name = "Làn đua", description = "API gán làn đua cho đăng ký thi đấu")
public class LaneController {

    private final RegistrationService registrationService;
    private final CurrentUserService currentUserService;

    @PostMapping
    public ResponseEntity<ApiResponse<LaneResponseDTO>> assignLane(
            @Valid @RequestBody LaneRequestDTO dto, Authentication authentication) {
        String staffId = currentUserService.resolveMaTK(authentication);
        LaneResponseDTO created = registrationService.assignLane(dto, staffId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created, "Đã gán làn đua"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LaneResponseDTO>> updateLane(
            @PathVariable String id, @Valid @RequestBody LaneRequestDTO dto, Authentication authentication) {
        String staffId = currentUserService.resolveMaTK(authentication);
        return ResponseEntity.ok(ApiResponse.success(registrationService.updateLane(id, dto, staffId), "Đã cập nhật làn đua"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> removeLane(@PathVariable String id, Authentication authentication) {
        String staffId = currentUserService.resolveMaTK(authentication);
        registrationService.removeLane(id, staffId);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã bỏ gán làn đua"));
    }
}
