package edu.cit.sala.patupi.controller;

import edu.cit.sala.patupi.entity.Appointment;
import edu.cit.sala.patupi.entity.User;
import edu.cit.sala.patupi.repository.AppointmentRepository;
import edu.cit.sala.patupi.repository.ServiceRepository;
import edu.cit.sala.patupi.repository.UserRepository;
import edu.cit.sala.patupi.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import edu.cit.sala.patupi.dto.AppointmentResponseDTO;
import edu.cit.sala.patupi.service.NotificationFactory;


import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = {"http://localhost:5173", "http://192.168.1.9:8080", "http://192.168.1.9"})
public class AppointmentController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private NotificationFactory notificationFactory;


    @GetMapping
    public ResponseEntity<?> getAllAppointments() {
        return ResponseEntity.ok(appointmentRepository.findAll());
    }

@PostMapping("/step1")
public ResponseEntity<AppointmentResponseDTO> startBooking(@RequestBody Map<String, Object> payload) {
    try {
        Long customerId = Long.parseLong(payload.get("customerId").toString());
        User user = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Appointment appointment = new Appointment();
        appointment.setUser(user);
        appointment.setDescription((String) payload.get("description"));
        
        if (payload.get("serviceId") != null) {
            appointment.setService_id(Integer.parseInt(payload.get("serviceId").toString()));
        }

        Appointment saved = appointmentRepository.save(appointment);

        // STRUCTURAL PATTERN: DTO Pattern Implementation
        AppointmentResponseDTO dto = new AppointmentResponseDTO();
        dto.setAppointmentId(saved.getId()); 
        dto.setCustomerName(user.getFullName());
        dto.setStatus(saved.getStatus());
        dto.setDescription(saved.getDescription());
        
        return ResponseEntity.ok(dto);
    } catch (Exception e) {
        return ResponseEntity.status(500).build();
    }
}

    @PutMapping("/confirm/{id}")
    public ResponseEntity<?> confirmBooking(@PathVariable Long id, @RequestBody Appointment details) {
        return appointmentRepository.findById(id).map(appointment -> {
            appointment.setService_id(details.getService_id());
            appointment.setScheduled_at(details.getScheduled_at());
            
            if (details.getService_id() != null) {
                serviceRepository.findById(details.getService_id()).ifPresent(s -> {
                    appointment.setTotal_amount(s.getBase_price());
                });
            }
            appointmentRepository.save(appointment);
            return ResponseEntity.ok(Map.of("status", "success"));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<?> assignBarber(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return appointmentRepository.findById(id).map(appointment -> {
            Long barberId = Long.parseLong(payload.get("barberId").toString());
            appointment.setBarberId(barberId);
            appointment.setStatus("CONFIRMED");
            appointmentRepository.save(appointment);
            return ResponseEntity.ok(Map.of("message", "Barber assigned"));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{id}")
    public ResponseEntity<?> getCustomerAppointments(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getCustomerAppointmentsWithNames(id));
    }

    
    @GetMapping("/all")
    public ResponseEntity<?> getAllForAdmin() {
        return ResponseEntity.ok(appointmentService.getAllAppointmentsForAdmin());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAppointment(@PathVariable Long id) {
        return appointmentRepository.findById(id).map(appointment -> {
            appointmentRepository.delete(appointment);
            return ResponseEntity.ok(Map.of("message", "Appointment deleted successfully"));
        }).orElse(ResponseEntity.notFound().build());
    }

@PutMapping("/{id}/cancel")
public ResponseEntity<?> cancelAppointment(@PathVariable Long id, @RequestParam Long customerId) {
    return appointmentRepository.findById(id).map(appointment -> {
        if (appointment.getUser() == null || !appointment.getUser().getUserId().equals(customerId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
        }
        
        appointment.setStatus("CANCELLED");
        appointmentRepository.save(appointment);

        // BEHAVIORAL PATTERN: Using the Factory to notify the user
        try {
            notificationFactory.sendNotification(
                "EMAIL", 
                appointment.getUser().getEmail(), 
                "Patupi: Appointment Cancelled", 
                "Hi " + appointment.getUser().getFullName() + ", your appointment has been successfully cancelled."
            );
        } catch (Exception e) {
            System.out.println("Email failed but appointment was cancelled.");
        }

        return ResponseEntity.ok(Map.of("message", "Cancelled and notification sent"));
    }).orElse(ResponseEntity.notFound().build());
}
}