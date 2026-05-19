package com.example.etl.controller;

import com.example.etl.model.EtlHistory;
import com.example.etl.service.EtlService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/etl")
public class EtlController {

    private final EtlService etlService;

    public EtlController(EtlService etlService) {
        this.etlService = etlService;
    }

    @PostMapping("/run")
    public ResponseEntity<?> runEtl(@RequestBody Map<String, String> request) {
        String mode = request.get("mode");
        // Run in a separate thread to avoid timeout if it's long
        new Thread(() -> etlService.runEtl(mode)).start();
        return ResponseEntity.ok(Map.of("message", "ETL started", "mode", mode));
    }

    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        EtlHistory history = etlService.getLatestHistory();
        if (history == null) {
            return ResponseEntity.ok(Map.of("status", "NONE"));
        }
        return ResponseEntity.ok(Map.of(
            "status", history.getStatus(),
            "log", "Current status: " + history.getStatus(),
            "progress", history.getStatus().equals("SUCCESS") ? 100 : 50 // Mock progress
        ));
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getSummary() {
        EtlHistory history = etlService.getLatestHistory();
        if (history == null) {
            return ResponseEntity.ok(Map.of("status", "-"));
        }
        return ResponseEntity.ok(history);
    }

    @PostMapping("/schedule")
    public ResponseEntity<?> saveSchedule(@RequestBody Map<String, String> request) {
        String time = request.get("time");
        etlService.scheduleEtl(time);
        return ResponseEntity.ok(Map.of("message", "Schedule saved and activated", "time", time));
    }

    @DeleteMapping("/schedule")
    public ResponseEntity<?> cancelSchedule() {
        etlService.cancelSchedule();
        return ResponseEntity.ok(Map.of("message", "Schedule cancelled"));
    }
}
