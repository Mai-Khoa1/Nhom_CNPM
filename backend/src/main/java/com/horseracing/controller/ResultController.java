package com.horseracing.controller;

import com.horseracing.dto.common.ApiResponse;
import com.horseracing.dto.result.ResultEntryRequestDTO;
import com.horseracing.dto.result.ResultResponseDTO;
import com.horseracing.service.CurrentUserService;
import com.horseracing.service.ResultService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * ResultController - quản lý kết quả thi đấu (bảng KetQuaThiDau). Base path /results khớp frontend.
 */
@RestController
@RequestMapping("/results")
@RequiredArgsConstructor
@Tag(name = "Kết quả thi đấu", description = "API ghi nhận, cập nhật và công bố kết quả thi đấu")
public class ResultController {

    private final ResultService resultService;
    private final CurrentUserService currentUserService;

    @PostMapping
    public ResponseEntity<ApiResponse<ResultResponseDTO>> submitResults(
            @Valid @RequestBody ResultEntryRequestDTO dto, Authentication authentication) {
        String staffId = currentUserService.resolveMaTK(authentication);
        ResultResponseDTO created = resultService.submitResults(dto, staffId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created, "Ghi nhận kết quả thành công"));
    }

    @GetMapping("/{raceId}")
    public ResponseEntity<ApiResponse<ResultResponseDTO>> getResultsByRace(@PathVariable String raceId) {
        return ResponseEntity.ok(ApiResponse.success(resultService.getResultsByRace(raceId)));
    }

    @PutMapping("/{raceId}")
    public ResponseEntity<ApiResponse<ResultResponseDTO>> updateResults(
            @PathVariable String raceId, @Valid @RequestBody ResultEntryRequestDTO dto, Authentication authentication) {
        String staffId = currentUserService.resolveMaTK(authentication);
        dto.setRaceId(raceId);
        return ResponseEntity.ok(ApiResponse.success(resultService.submitResults(dto, staffId), "Cập nhật kết quả thành công"));
    }

    @PatchMapping("/{raceId}/publish")
    public ResponseEntity<ApiResponse<ResultResponseDTO>> publishResults(
            @PathVariable String raceId, Authentication authentication) {
        String staffId = currentUserService.resolveMaTK(authentication);
        return ResponseEntity.ok(ApiResponse.success(resultService.publishResults(raceId, staffId), "Đã công bố kết quả"));
    }
}
