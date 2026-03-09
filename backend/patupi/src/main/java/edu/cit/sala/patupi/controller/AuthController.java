package edu.cit.sala.patupi.controller;

import edu.cit.sala.patupi.entity.User;
import edu.cit.sala.patupi.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Crucial for React/Android connection
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register-profile")
    public User register(@RequestBody User user) {
        return authService.registerProfile(user);
    }

    @GetMapping("/me/{id}")
    public User getMe(@PathVariable UUID id) {
        return authService.getProfile(id);
    }
}