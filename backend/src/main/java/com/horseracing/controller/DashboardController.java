package com.horseracing.controller;

import com.horseracing.dto.common.ApiResponse;
import com.horseracing.dto.dashboard.DashboardStatsResponseDTO;
import com.horseracing.dto.schedule.ScheduleResponseDTO;
import com.horseracing.service.DashboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * DashboardController - thống kê tổng quan hệ thống. Base path /dashboard khớp frontend.
 */
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "API thống kê tổng quan")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponseDTO>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getStats()));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<ScheduleResponseDTO>>> getUpcoming() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getUpcomingRaces()));
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<Object>>> getRecent() {
        return ResponseEntity.ok(ApiResponse.success(Collections.emptyList()));
    }
}
