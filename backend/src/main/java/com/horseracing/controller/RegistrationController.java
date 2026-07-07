package com.horseracing.controller;

import com.horseracing.dto.common.ApiResponse;
import com.horseracing.dto.common.PageResponse;
import com.horseracing.dto.common.RegistrationStatus;
import com.horseracing.dto.registration.RegistrationRequestDTO;
import com.horseracing.dto.registration.RegistrationResponseDTO;
import com.horseracing.service.CurrentUserService;
import com.horseracing.service.RegistrationService;
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
 * RegistrationController - đăng ký thi đấu (DangKyThiDau). Base path /registrations khớp frontend.
 */
@RestController
@RequestMapping("/registrations")
@RequiredArgsConstructor
@Tag(name = "Đăng ký thi đấu", description = "API đăng ký, duyệt, từ chối đăng ký thi đấu")
public class RegistrationController {

    private final RegistrationService registrationService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<RegistrationResponseDTO>>> getAllRegistrations(
            Pageable pageable,
            @RequestParam(required = false) String raceId,
            @RequestParam(required = false) RegistrationStatus status,
            Authentication authentication) {
        String organizerScopeId = currentUserService.resolveOrganizerId(authentication);
        return ResponseEntity.ok(ApiResponse.success(registrationService.getAllRegistrations(pageable, raceId, status, organizerScopeId)));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PageResponse<RegistrationResponseDTO>>> getMyRegistrations(
            Pageable pageable, Authentication authentication) {
        String maTK = currentUserService.resolveMaTK(authentication);
        return ResponseEntity.ok(ApiResponse.success(registrationService.getMyRegistrations(maTK, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RegistrationResponseDTO>> getRegistrationById(@PathVariable String id, Authentication authentication) {
        String organizerScopeId = currentUserService.resolveOrganizerId(authentication);
        return ResponseEntity.ok(ApiResponse.success(registrationService.getRegistrationById(id, organizerScopeId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RegistrationResponseDTO>> createRegistration(
            @Valid @RequestBody RegistrationRequestDTO dto, Authentication authentication) {
        String maTK = currentUserService.resolveMaTK(authentication);
        RegistrationResponseDTO created = registrationService.createRegistration(dto, maTK, maTK);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created, "Đăng ký thi đấu thành công"));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approveRegistration(@PathVariable String id, Authentication authentication) {
        String staffId = currentUserService.resolveMaTK(authentication);
        String organizerScopeId = currentUserService.resolveOrganizerId(authentication);
        registrationService.approveRegistration(id, staffId, organizerScopeId);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã duyệt đăng ký"));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectRegistration(
            @PathVariable String id, @RequestBody(required = false) Map<String, String> body, Authentication authentication) {
        String staffId = currentUserService.resolveMaTK(authentication);
        String organizerScopeId = currentUserService.resolveOrganizerId(authentication);
        String reason = body != null ? body.get("reason") : null;
        registrationService.rejectRegistration(id, reason, staffId, organizerScopeId);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã từ chối đăng ký"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelRegistration(
            @PathVariable String id, @RequestBody(required = false) Map<String, String> body, Authentication authentication) {
        String maTK = currentUserService.resolveMaTK(authentication);
        String reason = body != null ? body.get("reason") : null;
        registrationService.cancelRegistration(id, reason, maTK, maTK);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã hủy đăng ký"));
    }

    @PatchMapping("/{id}/disqualify")
    public ResponseEntity<ApiResponse<RegistrationResponseDTO>> disqualifyRegistration(
            @PathVariable String id, @RequestBody(required = false) Map<String, String> body, Authentication authentication) {
        String staffId = currentUserService.resolveMaTK(authentication);
        String organizerScopeId = currentUserService.resolveOrganizerId(authentication);
        String reason = body != null ? body.get("reason") : null;
        boolean confirmRevokePublish = body != null && "true".equalsIgnoreCase(body.get("confirmRevokePublish"));
        return ResponseEntity.ok(ApiResponse.success(
                registrationService.disqualifyRegistration(id, reason, confirmRevokePublish, staffId, organizerScopeId), "Đã loại đăng ký"));
    }
}
