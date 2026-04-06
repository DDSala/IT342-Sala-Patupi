package edu.cit.sala.patupi.controller;

import edu.cit.sala.patupi.entity.User;
import edu.cit.sala.patupi.repository.UserRepository;
import edu.cit.sala.patupi.service.BarberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BarberService barberService;

    // Fetch all users for Admin Customer Management
    @GetMapping("/all")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    // Fetch only Barbers (Role 2)
    @GetMapping("/barbers")
    public ResponseEntity<?> getAllBarbers() {
        return ResponseEntity.ok(userRepository.findByRoleId(2));
    }

    // Update User Profile
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        return userRepository.findById(id).map(user -> {
            user.setFullName(userDetails.getFullName());
            user.setEmail(userDetails.getEmail());
            user.setAddress(userDetails.getAddress());
            User updatedUser = userRepository.save(user);
            return ResponseEntity.ok(updatedUser);
        }).orElse(ResponseEntity.notFound().build());
    }

    // Change Password
    @PutMapping("/{id}/password")
    public ResponseEntity<?> updatePassword(@PathVariable Long id, @RequestBody Map<String, String> passwords) {
        String currentPassword = passwords.get("currentPassword");
        String newPassword = passwords.get("newPassword");

        return userRepository.findById(id).map(user -> {
            if (passwordEncoder.matches(currentPassword, user.getPassword())) {
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                return ResponseEntity.ok().body(Map.of("message", "Password updated successfully"));
            } else {
                return ResponseEntity.status(401).body(Map.of("message", "Incorrect current password"));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    // Register/Onboard a new Barber
    @PostMapping("/register-barber")
    public ResponseEntity<?> registerBarber(@RequestBody Map<String, String> payload) {
        try {
            barberService.onboardBarber(payload);
            return ResponseEntity.ok(Map.of("message", "Barber onboarded successfully!"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // The "God Mode" Delete Button
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        return userRepository.findById(id).map(user -> {
            userRepository.delete(user);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found")));
    }
}