package edu.cit.sala.patupi.service;

import edu.cit.sala.patupi.entity.User;
import edu.cit.sala.patupi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    public User registerProfile(User user) {
        // Logic: Default to Customer (Role 3) if not specified
        if (user.getRoleId() == null) user.setRoleId(3);
        return userRepository.save(user);
    }

    public User getProfile(UUID id) {
        return userRepository.findById(id).orElse(null);
    }
}