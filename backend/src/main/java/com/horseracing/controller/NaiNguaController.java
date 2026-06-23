package com.horseracing.controller;

import com.horseracing.dto.jockey.JockeyRequestDTO;
import com.horseracing.dto.jockey.JockeyResponseDTO;
import com.horseracing.dto.jockey.JockeyStatsDTO;
import com.horseracing.service.JockeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * NaiNguaController - REST API quản lý nài ngựa (Jockey)
 * Base path: /api/jockey
 */
@RestController
@RequestMapping("/api/jockey")
@RequiredArgsConstructor
@Tag(name = "Quản lý Jockey", description = "API thêm, sửa, xóa, cập nhật chỉ số và tìm kiếm nài ngựa")
public class NaiNguaController {

    private final JockeyService jockeyService;

    // Header name dùng để truyền staffId từ frontend (hoặc lấy từ JWT context)
    private static final String STAFF_ID_HEADER = "X-Staff-Id";
    private static final String DEFAULT_STAFF = "SYSTEM";

    /**
     * Lấy tất cả jockey
     * GET /api/jockey
     */
    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả jockey")
    public ResponseEntity<List<JockeyResponseDTO>> getAllJockeys() {
        return ResponseEntity.ok(jockeyService.getAllJockeys());
    }

    /**
     * Lấy jockey theo ID
     * GET /api/jockey/{maNaiNgua}
     */
    @GetMapping("/{maNaiNgua}")
    @Operation(summary = "Lấy thông tin jockey theo mã")
    public ResponseEntity<JockeyResponseDTO> getJockeyById(@PathVariable String maNaiNgua) {
        return ResponseEntity.ok(jockeyService.getJockeyById(maNaiNgua));
    }

    /**
     * Tìm kiếm + lọc jockey
     * GET /api/jockey/search?hoTen=...&trangThai=...&kinhNghiemMin=...
     */
    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm và lọc jockey theo tên, trạng thái, kinh nghiệm")
    public ResponseEntity<List<JockeyResponseDTO>> searchJockeys(
            @RequestParam(required = false) String hoTen,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) Integer kinhNghiemMin) {
        return ResponseEntity.ok(jockeyService.searchJockeys(hoTen, trangThai, kinhNghiemMin));
    }

    /**
     * Tạo mới jockey
     * POST /api/jockey
     */
    @PostMapping
    @Operation(summary = "Thêm mới nài ngựa (jockey)")
    public ResponseEntity<JockeyResponseDTO> createJockey(
            @Valid @RequestBody JockeyRequestDTO dto,
            @RequestHeader(value = STAFF_ID_HEADER, defaultValue = DEFAULT_STAFF) String staffId) {
        JockeyResponseDTO created = jockeyService.createJockey(dto, staffId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Cập nhật thông tin jockey
     * PUT /api/jockey/{maNaiNgua}
     */
    @PutMapping("/{maNaiNgua}")
    @Operation(summary = "Cập nhật thông tin jockey")
    public ResponseEntity<JockeyResponseDTO> updateJockey(
            @PathVariable String maNaiNgua,
            @Valid @RequestBody JockeyRequestDTO dto,
            @RequestHeader(value = STAFF_ID_HEADER, defaultValue = DEFAULT_STAFF) String staffId) {
        return ResponseEntity.ok(jockeyService.updateJockey(maNaiNgua, dto, staffId));
    }

    /**
     * Cập nhật chỉ số sức khỏe / thống kê jockey
     * PATCH /api/jockey/{maNaiNgua}/stats
     */
    @PatchMapping("/{maNaiNgua}/stats")
    @Operation(summary = "Cập nhật chỉ số sức khỏe và thống kê của jockey")
    public ResponseEntity<JockeyResponseDTO> updateStats(
            @PathVariable String maNaiNgua,
            @Valid @RequestBody JockeyStatsDTO statsDTO,
            @RequestHeader(value = STAFF_ID_HEADER, defaultValue = DEFAULT_STAFF) String staffId) {
        return ResponseEntity.ok(jockeyService.updateStats(maNaiNgua, statsDTO, staffId));
    }

    /**
     * Xóa jockey (kiểm tra lịch đua trước, nếu có thì vô hiệu hóa)
     * DELETE /api/jockey/{maNaiNgua}
     */
    @DeleteMapping("/{maNaiNgua}")
    @Operation(summary = "Xóa jockey (vô hiệu hóa nếu đang có lịch thi đấu)")
    public ResponseEntity<Map<String, String>> deleteJockey(
            @PathVariable String maNaiNgua,
            @RequestHeader(value = STAFF_ID_HEADER, defaultValue = DEFAULT_STAFF) String staffId) {
        jockeyService.deleteJockey(maNaiNgua, staffId);
        return ResponseEntity.ok(Map.of("message", "Xóa jockey thành công"));
    }
}
