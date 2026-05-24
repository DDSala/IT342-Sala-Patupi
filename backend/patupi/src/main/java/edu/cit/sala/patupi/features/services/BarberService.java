package edu.cit.sala.patupi.features.services;

import edu.cit.sala.patupi.features.authentication.User;
import edu.cit.sala.patupi.features.authentication.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; 
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

@Service
public class BarberService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; 

    @Autowired
    private BarberProfileRepository barberProfileRepository;

    @Transactional
    public void onboardBarber(Map<String, String> data) {
        User user = new User();
        user.setFullName(data.get("firstName") + " " + data.get("lastName"));
        user.setEmail(data.get("email"));
        user.setAddress(data.get("address"));
        user.setRoleId(2); 
        user.setPassword(passwordEncoder.encode("Patupi123!")); 

        BarberProfile profile = new BarberProfile();
        
        // 🌟 FIXED: Read the status from the payload map, defaulting to "Unavailable" if empty
        String initialStatus = data.getOrDefault("status", "Unavailable");
        profile.setStatus(initialStatus);
        
        profile.setUser(user);
        user.setBarberProfile(profile);
        
        userRepository.save(user);
    }

    // 🌟 FIXED METHOD: Safely maps the target profile using the User relationship ID
    @Transactional
    public boolean updateStatus(Long barberId, String status) {
        return userRepository.findById(barberId)
            .map(user -> {
                BarberProfile profile = user.getBarberProfile();
                if (profile != null) {
                    profile.setStatus(status);
                    barberProfileRepository.save(profile); // Explicit save to commit the child status row
                    return true;
                }
                return false;
            })
            .orElse(false);
    }
}