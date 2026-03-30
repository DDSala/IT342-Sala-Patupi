package edu.cit.sala.patupi.controller;

import edu.cit.sala.patupi.entity.Service;
import edu.cit.sala.patupi.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/services")
@CrossOrigin(origins = "http://localhost:5173")
public class ServiceController {

    @Autowired
    private ServiceRepository serviceRepository;

    @GetMapping
    public List<Service> getMenu() {
        return serviceRepository.findAll();
    }
}