package com.horseracing.controller;

import com.horseracing.dto.chungua.ChuNguaRequestDTO;
import com.horseracing.dto.chungua.ChuNguaResponseDTO;
import com.horseracing.dto.common.ApiResponse;
import com.horseracing.service.ChuNguaService;
import com.horseracing.service.CurrentUserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ChuNguaController - quản lý hồ sơ chủ ngựa.
 */
@RestController
@RequestMapping("/chu-ngua")
@RequiredArgsConstructor
@Tag(name = "Quản lý Chủ ngựa", description = "API quản lý hồ sơ chủ ngựa")
public class ChuNguaController {

    private final ChuNguaService chuNguaService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ChuNguaResponseDTO>>> getAllChuNgua() {
        return ResponseEntity.ok(ApiResponse.success(chuNguaService.getAllChuNgua()));
    }

    @GetMapping("/{maChuNgua}")
    public ResponseEntity<ApiResponse<ChuNguaResponseDTO>> getChuNguaById(@PathVariable String maChuNgua) {
        return ResponseEntity.ok(ApiResponse.success(chuNguaService.getChuNguaById(maChuNgua)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ChuNguaResponseDTO>> getMyProfile(Authentication authentication) {
        String maTK = currentUserService.resolveMaTK(authentication);
        return ResponseEntity.ok(ApiResponse.success(chuNguaService.getChuNguaByMaTK(maTK)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ChuNguaResponseDTO>> createChuNgua(
            @Valid @RequestBody ChuNguaRequestDTO dto, Authentication authentication) {
        String maTK = currentUserService.resolveMaTK(authentication);
        ChuNguaResponseDTO created = chuNguaService.createChuNgua(dto, maTK);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created, "Tạo hồ sơ chủ ngựa thành công"));
    }

    @PutMapping("/{maChuNgua}")
    public ResponseEntity<ApiResponse<ChuNguaResponseDTO>> updateChuNgua(
            @PathVariable String maChuNgua, @Valid @RequestBody ChuNguaRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(chuNguaService.updateChuNgua(maChuNgua, dto)));
    }

    @DeleteMapping("/{maChuNgua}")
    public ResponseEntity<ApiResponse<Void>> deleteChuNgua(@PathVariable String maChuNgua) {
        chuNguaService.deleteChuNgua(maChuNgua);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã xóa hồ sơ chủ ngựa"));
    }
}
