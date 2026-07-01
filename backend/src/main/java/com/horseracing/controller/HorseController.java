package com.horseracing.controller;

import com.horseracing.entity.Horse;
import com.horseracing.service.HorseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Horse Management Controller
 */
@RestController
@RequestMapping("/horses")
public class HorseController {
    
    @Autowired
    private HorseService horseService;
    
    /**
     * Get all horses
     * GET /horses
     */
    @GetMapping
    public ResponseEntity<List<Horse>> getAllHorses() {
        return ResponseEntity.ok(horseService.getAllHorses());
    }
    
    /**
     * Get horse by ID
     * GET /horses/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Horse> getHorseById(@PathVariable Long id) {
        return horseService.getHorseById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Create new horse
     * POST /horses
     */
    @PostMapping
    public ResponseEntity<Horse> createHorse(@RequestBody Horse horse) {
        Horse created = horseService.createHorse(horse);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    /**
     * Update horse
     * PUT /horses/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Horse> updateHorse(@PathVariable Long id, @RequestBody Horse horse) {
        Horse updated = horseService.updateHorse(id, horse);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * Delete horse
     * DELETE /horses/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHorse(@PathVariable Long id) {
        horseService.deleteHorse(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get horses by status
     * GET /horses/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Horse>> getHorsesByStatus(@PathVariable String status) {
        return ResponseEntity.ok(horseService.getHorsesByStatus(status));
    }
}
