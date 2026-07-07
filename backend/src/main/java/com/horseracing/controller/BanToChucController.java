package com.horseracing.controller;

import com.horseracing.dto.common.ApiResponse;
import com.horseracing.dto.organizer.OrganizerResponseDTO;
import com.horseracing.service.BanToChucService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * BanToChucController - danh sách Ban tổ chức. Base path /organizers khớp frontend.
 * Dùng để chủ ngựa chọn Ban tổ chức khi tạo ngựa/nài/đăng ký thi đấu.
 */
@RestController
@RequestMapping("/organizers")
@RequiredArgsConstructor
@Tag(name = "Ban tổ chức", description = "API danh sách Ban tổ chức")
public class BanToChucController {

    private final BanToChucService banToChucService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrganizerResponseDTO>>> getAllOrganizers() {
        return ResponseEntity.ok(ApiResponse.success(banToChucService.getAllOrganizers()));
    }
}
