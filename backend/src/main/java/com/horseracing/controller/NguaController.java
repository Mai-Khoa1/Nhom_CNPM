package com.horseracing.controller;

import com.horseracing.dto.horse.HorseRequestDTO;
import com.horseracing.dto.horse.HorseResponseDTO;
import com.horseracing.service.HorseService;
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
 * NguaController - REST API quản lý ngựa đua
 * Base path: /api/ngua
 */
@RestController
@RequestMapping("/api/ngua")
@RequiredArgsConstructor
@Tag(name = "Quản lý Ngựa", description = "API thêm, sửa, xóa và tìm kiếm ngựa đua")
public class NguaController {

    private final HorseService horseService;

    /**
     * Lấy tất cả ngựa
     * GET /api/ngua
     */
    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả ngựa đua")
    public ResponseEntity<List<HorseResponseDTO>> getAllHorses() {
        return ResponseEntity.ok(horseService.getAllHorses());
    }

    /**
     * Lấy ngựa theo ID
     * GET /api/ngua/{maNgua}
     */
    @GetMapping("/{maNgua}")
    @Operation(summary = "Lấy thông tin ngựa theo mã")
    public ResponseEntity<HorseResponseDTO> getHorseById(@PathVariable String maNgua) {
        return ResponseEntity.ok(horseService.getHorseById(maNgua));
    }

    /**
     * Tìm kiếm + lọc ngựa
     * GET /api/ngua/search?tenNgua=...&trangThai=...&maChuNgua=...
     */
    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm và lọc ngựa theo tên, trạng thái, chủ ngựa")
    public ResponseEntity<List<HorseResponseDTO>> searchHorses(
            @RequestParam(required = false) String tenNgua,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) String maChuNgua) {
        return ResponseEntity.ok(horseService.searchHorses(tenNgua, trangThai, maChuNgua));
    }

    /**
     * Lấy ngựa theo chủ ngựa
     * GET /api/ngua/owner/{maChuNgua}
     */
    @GetMapping("/owner/{maChuNgua}")
    @Operation(summary = "Lấy danh sách ngựa theo chủ ngựa")
    public ResponseEntity<List<HorseResponseDTO>> getHorsesByOwner(@PathVariable String maChuNgua) {
        return ResponseEntity.ok(horseService.getHorsesByOwner(maChuNgua));
    }

    /**
     * Tạo mới ngựa đua
     * POST /api/ngua
     */
    @PostMapping
    @Operation(summary = "Thêm mới ngựa đua")
    public ResponseEntity<HorseResponseDTO> createHorse(@Valid @RequestBody HorseRequestDTO dto) {
        HorseResponseDTO created = horseService.createHorse(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Cập nhật thông tin ngựa
     * PUT /api/ngua/{maNgua}
     */
    @PutMapping("/{maNgua}")
    @Operation(summary = "Cập nhật thông tin ngựa đua")
    public ResponseEntity<HorseResponseDTO> updateHorse(
            @PathVariable String maNgua,
            @Valid @RequestBody HorseRequestDTO dto) {
        return ResponseEntity.ok(horseService.updateHorse(maNgua, dto));
    }

    /**
     * Xóa ngựa (kiểm tra lịch đua trước)
     * DELETE /api/ngua/{maNgua}
     */
    @DeleteMapping("/{maNgua}")
    @Operation(summary = "Xóa ngựa đua (không xóa nếu đang có lịch thi đấu)")
    public ResponseEntity<Map<String, String>> deleteHorse(@PathVariable String maNgua) {
        horseService.deleteHorse(maNgua);
        return ResponseEntity.ok(Map.of("message", "Xóa ngựa thành công"));
    }
}
