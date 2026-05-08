package edu.cit.sala.patupi.features.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@CrossOrigin(origins = {"http://localhost:5173", "http://192.168.1.9:8080", "http://192.168.1.9"})
public class ServiceController {

    @Autowired
    private ServiceRepository serviceRepository;

    @GetMapping
    public ResponseEntity<List<Service>> getMenu() {
        List<Service> services = serviceRepository.findAll();
        if (services.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(services);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Service> getServiceById(@PathVariable Integer id) {
        return serviceRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}