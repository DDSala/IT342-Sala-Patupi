package edu.cit.sala.patupi.service;

import edu.cit.sala.patupi.entity.User;
import edu.cit.sala.patupi.entity.BarberProfile;
import edu.cit.sala.patupi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // Ensure you have this dependency
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

@Service
public class BarberService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Added to fix the "Encoded password" warning

    @Transactional
    public void onboardBarber(Map<String, String> data) {
        User user = new User();
        user.setFullName(data.get("firstName") + " " + data.get("lastName"));
        user.setEmail(data.get("email"));
        user.setAddress(data.get("address"));
        user.setRoleId(2); 
        
        // FIX: Encode the password so Spring Security can read it
        user.setPassword(passwordEncoder.encode("Patupi123!")); 

        BarberProfile profile = new BarberProfile();
        profile.setStatus("Available");
        
        // FIX: Bidirectional Link
        profile.setUser(user);
        user.setBarberProfile(profile);
        
        // Save user (Cascades to profile automatically)
        userRepository.save(user);
    }
}