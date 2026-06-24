package com.horseracing.controller;

import com.horseracing.dto.common.ApiResponse;
import com.horseracing.dto.nhatky.NhatKyHoatDongResponseDTO;
import com.horseracing.service.NhatKyHoatDongService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * NhatKyHoatDongController - xem nhật ký hoạt động (audit log) của hệ thống.
 */
@RestController
@RequestMapping("/nhat-ky-hoat-dong")
@RequiredArgsConstructor
@Tag(name = "Nhật ký hoạt động", description = "API xem audit log hệ thống")
public class NhatKyHoatDongController {

    private final NhatKyHoatDongService nhatKyHoatDongService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NhatKyHoatDongResponseDTO>>> getAllLogs() {
        return ResponseEntity.ok(ApiResponse.success(nhatKyHoatDongService.getAllLogs()));
    }

    @GetMapping("/user/{maTK}")
    public ResponseEntity<ApiResponse<List<NhatKyHoatDongResponseDTO>>> getLogsByUser(@PathVariable String maTK) {
        return ResponseEntity.ok(ApiResponse.success(nhatKyHoatDongService.getLogsByUser(maTK)));
    }

    @GetMapping("/action/{loaiHanhDong}")
    public ResponseEntity<ApiResponse<List<NhatKyHoatDongResponseDTO>>> getLogsByAction(@PathVariable String loaiHanhDong) {
        return ResponseEntity.ok(ApiResponse.success(nhatKyHoatDongService.getLogsByAction(loaiHanhDong)));
    }
}
