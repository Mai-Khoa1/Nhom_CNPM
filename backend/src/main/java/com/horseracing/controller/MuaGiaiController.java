package com.horseracing.controller;

import com.horseracing.dto.common.ApiResponse;
import com.horseracing.dto.common.PageResponse;
import com.horseracing.dto.common.SeasonStatus;
import com.horseracing.dto.season.PointRuleRequestDTO;
import com.horseracing.dto.season.PointRuleResponseDTO;
import com.horseracing.dto.season.SeasonRequestDTO;
import com.horseracing.dto.season.SeasonResponseDTO;
import com.horseracing.service.CurrentUserService;
import com.horseracing.service.MuaGiaiService;
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
 * MuaGiaiController - quản lý mùa giải (Season). Base path /seasons khớp frontend.
 */
@RestController
@RequestMapping("/seasons")
@RequiredArgsConstructor
@Tag(name = "Mùa giải", description = "API quản lý mùa giải và luật tính điểm")
public class MuaGiaiController {

    private final MuaGiaiService muaGiaiService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SeasonResponseDTO>>> getAllSeasons(
            Pageable pageable,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) SeasonStatus status) {
        return ResponseEntity.ok(ApiResponse.success(muaGiaiService.getAllSeasons(pageable, keyword, status)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SeasonResponseDTO>> getSeasonById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(muaGiaiService.getSeasonById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SeasonResponseDTO>> createSeason(
            @Valid @RequestBody SeasonRequestDTO dto, Authentication authentication) {
        String staffId = currentUserService.resolveMaTK(authentication);
        SeasonResponseDTO created = muaGiaiService.createSeason(dto, staffId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created, "Tạo mùa giải thành công"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SeasonResponseDTO>> updateSeason(
            @PathVariable String id, @Valid @RequestBody SeasonRequestDTO dto, Authentication authentication) {
        String staffId = currentUserService.resolveMaTK(authentication);
        return ResponseEntity.ok(ApiResponse.success(muaGiaiService.updateSeason(id, dto, staffId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSeason(@PathVariable String id, Authentication authentication) {
        String staffId = currentUserService.resolveMaTK(authentication);
        muaGiaiService.deleteSeason(id, staffId);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã xóa mùa giải"));
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<ApiResponse<Void>> closeSeason(@PathVariable String id, Authentication authentication) {
        String staffId = currentUserService.resolveMaTK(authentication);
        muaGiaiService.closeSeason(id, staffId);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã đóng mùa giải"));
    }

    @GetMapping("/{id}/point-rules")
    public ResponseEntity<ApiResponse<List<PointRuleResponseDTO>>> getPointRules(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(muaGiaiService.getPointRules(id)));
    }

    @PutMapping("/{id}/point-rules")
    public ResponseEntity<ApiResponse<List<PointRuleResponseDTO>>> setPointRules(
            @PathVariable String id, @Valid @RequestBody List<PointRuleRequestDTO> rules, Authentication authentication) {
        String staffId = currentUserService.resolveMaTK(authentication);
        return ResponseEntity.ok(ApiResponse.success(muaGiaiService.setPointRules(id, rules, staffId), "Đã cập nhật luật tính điểm"));
    }
}
