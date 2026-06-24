package com.horseracing.controller;

import com.horseracing.entity.Schedule;
import com.horseracing.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Race Schedule Management Controller
 */
@RestController
@RequestMapping("/schedules")
public class ScheduleController {
    
    @Autowired
    private ScheduleService scheduleService;
    
    /**
     * Get all schedules
     * GET /schedules
     */
    @GetMapping
    public ResponseEntity<List<Schedule>> getAllSchedules() {
        return ResponseEntity.ok(scheduleService.getAllSchedules());
    }
    
    /**
     * Get schedule by ID
     * GET /schedules/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Schedule> getScheduleById(@PathVariable Long id) {
        return scheduleService.getScheduleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Create new schedule
     * POST /schedules
     */
    @PostMapping
    public ResponseEntity<Schedule> createSchedule(@RequestBody Schedule schedule) {
        Schedule created = scheduleService.createSchedule(schedule);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    /**
     * Update schedule
     * PUT /schedules/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Schedule> updateSchedule(@PathVariable Long id, @RequestBody Schedule schedule) {
        Schedule updated = scheduleService.updateSchedule(id, schedule);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * Delete schedule
     * DELETE /schedules/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get schedules by status
     * GET /schedules/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Schedule>> getSchedulesByStatus(@PathVariable String status) {
        return ResponseEntity.ok(scheduleService.getSchedulesByStatus(status));
    }
}
