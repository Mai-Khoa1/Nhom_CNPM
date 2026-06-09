package com.horseracing.controller;

import com.horseracing.entity.Jockey;
import com.horseracing.service.JockeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Jockey Management Controller
 */
@RestController
@RequestMapping("/jockeys")
public class JockeyController {
    
    @Autowired
    private JockeyService jockeyService;
    
    /**
     * Get all jockeys
     * GET /jockeys
     */
    @GetMapping
    public ResponseEntity<List<Jockey>> getAllJockeys() {
        return ResponseEntity.ok(jockeyService.getAllJockeys());
    }
    
    /**
     * Get jockey by ID
     * GET /jockeys/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Jockey> getJockeyById(@PathVariable Long id) {
        return jockeyService.getJockeyById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Create new jockey
     * POST /jockeys
     */
    @PostMapping
    public ResponseEntity<Jockey> createJockey(@RequestBody Jockey jockey) {
        Jockey created = jockeyService.createJockey(jockey);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    /**
     * Update jockey
     * PUT /jockeys/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Jockey> updateJockey(@PathVariable Long id, @RequestBody Jockey jockey) {
        Jockey updated = jockeyService.updateJockey(id, jockey);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * Delete jockey
     * DELETE /jockeys/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJockey(@PathVariable Long id) {
        jockeyService.deleteJockey(id);
        return ResponseEntity.noContent().build();
    }
}
