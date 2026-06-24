package com.horseracing.controller;

import com.horseracing.dto.common.ApiResponse;
import com.horseracing.dto.common.PageResponse;
import com.horseracing.dto.ranking.HorseRankingResponseDTO;
import com.horseracing.dto.ranking.JockeyRankingResponseDTO;
import com.horseracing.service.RankingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * RankingController - bảng xếp hạng ngựa/jockey theo mùa giải. Base path /rankings khớp frontend.
 */
@RestController
@RequestMapping("/rankings")
@RequiredArgsConstructor
@Tag(name = "Bảng xếp hạng", description = "API bảng xếp hạng ngựa và jockey theo mùa giải")
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/horses")
    public ResponseEntity<ApiResponse<PageResponse<HorseRankingResponseDTO>>> getHorseRanking(
            @RequestParam(required = false) String seasonId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(rankingService.getHorseRanking(seasonId, pageable)));
    }

    @GetMapping("/jockeys")
    public ResponseEntity<ApiResponse<PageResponse<JockeyRankingResponseDTO>>> getJockeyRanking(
            @RequestParam(required = false) String seasonId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(rankingService.getJockeyRanking(seasonId, pageable)));
    }
}
