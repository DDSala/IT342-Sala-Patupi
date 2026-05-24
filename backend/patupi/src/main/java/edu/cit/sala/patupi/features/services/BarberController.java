package edu.cit.sala.patupi.features.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/barbers")
@CrossOrigin(originPatterns = "*", allowCredentials = "true") // 🌟 FIXED: Allows credentialed requests without crashing
public class BarberController {

    @Autowired
    private BarberService barberService;

    /**
     * PUT /api/barbers/{barberId}/status
     * Triggered by Android or Admin web when a barber shifts states (Available / Busy / Unavailable)
     */
    @PutMapping("/{barberId}/status")
    public ResponseEntity<?> changeBarberStatus(
            @PathVariable Long barberId, 
            @RequestBody Map<String, String> payload) {
        
        String status = payload.get("status");
        if (status == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing 'status' key in request body"));
        }

        boolean updated = barberService.updateStatus(barberId, status);
        
        if (updated) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Status shifted to " + status
            ));
        } else {
            return ResponseEntity.status(404).body(Map.of("error", "Barber profile not found with ID: " + barberId));
        }
    }
}