package com.horseracing.controller;

import com.horseracing.dto.chungua.ChuNguaRequestDTO;
import com.horseracing.dto.chungua.ChuNguaResponseDTO;
import com.horseracing.service.ChuNguaService;
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
 * ChuNguaController - REST API quản lý chủ ngựa
 * Base path: /api/chungua
 */
@RestController
@RequestMapping("/api/chungua")
@RequiredArgsConstructor
@Tag(name = "Quản lý Chủ Ngựa", description = "API thêm, sửa, xóa và tìm kiếm chủ ngựa")
public class ChuNguaController {

    private final ChuNguaService chuNguaService;

    /**
     * Lấy tất cả chủ ngựa
     * GET /api/chungua
     */
    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả chủ ngựa")
    public ResponseEntity<List<ChuNguaResponseDTO>> getAllChuNgua() {
        return ResponseEntity.ok(chuNguaService.getAllChuNgua());
    }

    /**
     * Lấy chủ ngựa theo ID
     * GET /api/chungua/{maChuNgua}
     */
    @GetMapping("/{maChuNgua}")
    @Operation(summary = "Lấy thông tin chủ ngựa theo mã")
    public ResponseEntity<ChuNguaResponseDTO> getChuNguaById(@PathVariable String maChuNgua) {
        return ResponseEntity.ok(chuNguaService.getChuNguaById(maChuNgua));
    }

    /**
     * Tìm kiếm chủ ngựa theo họ tên
     * GET /api/chungua/search?hoTen=...
     */
    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm chủ ngựa theo họ tên")
    public ResponseEntity<List<ChuNguaResponseDTO>> searchChuNgua(
            @RequestParam(required = false) String hoTen) {
        return ResponseEntity.ok(chuNguaService.searchChuNgua(hoTen));
    }

    /**
     * Lấy chủ ngựa theo email
     * GET /api/chungua/email/{email}
     */
    @GetMapping("/email/{email}")
    @Operation(summary = "Lấy thông tin chủ ngựa theo email")
    public ResponseEntity<ChuNguaResponseDTO> getChuNguaByEmail(@PathVariable String email) {
        return ResponseEntity.ok(chuNguaService.getChuNguaByEmail(email));
    }

    /**
     * Tạo mới chủ ngựa
     * POST /api/chungua
     */
    @PostMapping
    @Operation(summary = "Thêm mới chủ ngựa")
    public ResponseEntity<ChuNguaResponseDTO> createChuNgua(@Valid @RequestBody ChuNguaRequestDTO dto) {
        ChuNguaResponseDTO created = chuNguaService.createChuNgua(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Cập nhật thông tin chủ ngựa
     * PUT /api/chungua/{maChuNgua}
     */
    @PutMapping("/{maChuNgua}")
    @Operation(summary = "Cập nhật thông tin chủ ngựa")
    public ResponseEntity<ChuNguaResponseDTO> updateChuNgua(
            @PathVariable String maChuNgua,
            @Valid @RequestBody ChuNguaRequestDTO dto) {
        return ResponseEntity.ok(chuNguaService.updateChuNgua(maChuNgua, dto));
    }

    /**
     * Xóa chủ ngựa (kiểm tra xem có ngựa nào thuộc chủ này không)
     * DELETE /api/chungua/{maChuNgua}
     */
    @DeleteMapping("/{maChuNgua}")
    @Operation(summary = "Xóa chủ ngựa (không xóa nếu có ngựa thuộc chủ này)")
    public ResponseEntity<Map<String, String>> deleteChuNgua(@PathVariable String maChuNgua) {
        chuNguaService.deleteChuNgua(maChuNgua);
        return ResponseEntity.ok(Map.of("message", "Xóa chủ ngựa thành công"));
    }
}
