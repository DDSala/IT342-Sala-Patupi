package edu.cit.sala.patupi.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "barber_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BarberProfile {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    @JsonBackReference 
    private User user;

    private String status;

    // --- MANUAL SETTERS TO FIX COMPILATION ERRORS ---
    
    public void setStatus(String status) {
        this.status = status;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // Adding getters manually too just in case
    public String getStatus() {
        return status;
    }

    public User getUser() {
        return user;
    }
    
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}