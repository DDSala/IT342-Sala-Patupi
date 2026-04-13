package edu.cit.sala.patupi.controller;

import edu.cit.sala.patupi.dto.GoogleAuthRequest;
import edu.cit.sala.patupi.entity.User;
import edu.cit.sala.patupi.repository.UserRepository;
import edu.cit.sala.patupi.service.AuthService;
import edu.cit.sala.patupi.service.EmailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:5173", "http://192.168.1.9:8080", "http://192.168.1.9"})
public class AuthController {
    
    @Value("${google.client.id}")
    private String googleClientId;
    
    @Autowired
    private EmailService emailService;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping({"/register", "/register-profile"})
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            return ResponseEntity.ok(authService.registerProfile(user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            User user = authService.login(credentials.get("email"), credentials.get("password"));
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleAuthRequest request) {
        try {
            NetHttpTransport transport = new NetHttpTransport();
            GsonFactory jsonFactory = GsonFactory.getDefaultInstance();

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(request.getToken());

            if (idToken != null) {
                Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String name = (String) payload.get("name");

            User user = userRepository.findByEmail(email).orElseGet(() -> {
                User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setFullName(name);
                    newUser.setRoleId(3); 
    

                String rawPassword = java.util.UUID.randomUUID().toString();
                newUser.setPassword(passwordEncoder.encode(rawPassword)); 
    
                return userRepository.save(newUser);
            });

                String otp = String.format("%06d", new java.util.Random().nextInt(1000000));
                user.setOtpCode(otp);
                user.setOtpExpiry(java.time.LocalDateTime.now().plusMinutes(5));
                userRepository.save(user);

                emailService.sendOtpEmail(email, otp);

                return ResponseEntity.ok(Map.of("status", "PENDING_OTP", "email", email));
            }
            return ResponseEntity.status(401).body(Map.of("message", "Invalid Google Token"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Server Error"));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
    try {
        String email = request.get("email");
        String code = request.get("otp");


        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));


        if (user.getOtpCode() != null && 
            user.getOtpCode().equals(code) && 
            user.getOtpExpiry().isAfter(java.time.LocalDateTime.now())) {
            

            user.setOtpCode(null);
            user.setOtpExpiry(null);
            userRepository.save(user);


            return ResponseEntity.ok(user);
        } else {

            return ResponseEntity.status(401).body(Map.of("message", "Invalid or expired OTP code."));
        }
    } catch (Exception e) {
        return ResponseEntity.status(500).body(Map.of("message", "Verification Error: " + e.getMessage()));
    }
}

}