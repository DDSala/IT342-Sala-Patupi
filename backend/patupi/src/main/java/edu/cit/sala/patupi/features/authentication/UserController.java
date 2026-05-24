package edu.cit.sala.patupi.features.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import edu.cit.sala.patupi.features.services.BarberService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:5173", "http://192.168.1.2:8080", "http://192.168.1.2"})
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BarberService barberService;


    @GetMapping("/all")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    

    @GetMapping("/barbers")
    public ResponseEntity<?> getAllBarbers() {
        return ResponseEntity.ok(userRepository.findByRoleId(2));
    }


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

    @PostMapping("/register-barber")
    public ResponseEntity<?> registerBarber(@RequestBody Map<String, String> payload) {
        try {
            barberService.onboardBarber(payload);
            return ResponseEntity.ok(Map.of("message", "Barber onboarded successfully!"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        return userRepository.findById(id).map(user -> {
            userRepository.delete(user);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found")));
    }
}