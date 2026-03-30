package edu.cit.sala.patupi.controller;

import edu.cit.sala.patupi.entity.Appointment;
import edu.cit.sala.patupi.repository.AppointmentRepository;
import edu.cit.sala.patupi.service.AppointmentService; // Import the service
import edu.cit.sala.patupi.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "http://localhost:5173")
public class AppointmentController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @PostMapping("/step1")
    public ResponseEntity<?> startBooking(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "photoUrl", required = false) String photoUrl,
            @RequestParam("description") String description,
            @RequestParam("customerId") Long customerId) {
        try {
            Appointment appointment = new Appointment();
            appointment.setCustomer_id(customerId);
            appointment.setDescription(description);

            if (file != null && !file.isEmpty()) {
                String cloudUrl = cloudinaryService.uploadImage(file);
                appointment.setReference_photo(cloudUrl);
            } else {
                appointment.setReference_photo(photoUrl);
            }

            Appointment saved = appointmentRepository.save(appointment);
            return ResponseEntity.ok(Map.of("appointmentId", saved.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Step 1 Failed: " + e.getMessage());
        }
    }

    @PutMapping("/confirm/{id}")
    public ResponseEntity<?> confirmBooking(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        return appointmentRepository.findById(id).map(appointment -> {
            try {
                appointment.setService_id(Integer.parseInt(data.get("serviceId").toString()));
                appointment.setTotal_amount(new BigDecimal(data.get("totalAmount").toString()));

                appointment.setScheduled_at(LocalDateTime.parse(data.get("scheduledAt").toString()));
                
                appointment.setPayment_method("CASH_ONSITE");
                appointment.setStatus("CONFIRMED");

                appointmentRepository.save(appointment);
                return ResponseEntity.ok(Map.of("message", "Success"));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Finalization error: " + e.getMessage());
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/available-slots")
    public ResponseEntity<?> getOccupiedSlots(@RequestParam String date) {
        try {
            List<Appointment> booked = appointmentRepository.findBookedSlotsByDate(date);
            return ResponseEntity.ok(booked);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching slots: " + e.getMessage());
        }
    }

    @GetMapping("/customer/{id}")
    public ResponseEntity<?> getCustomerAppointments(@PathVariable Long id) {
        try {
            List<Map<String, Object>> appointments = appointmentService.getCustomerAppointmentsWithNames(id);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching appointments: " + e.getMessage());
        }
    }
}