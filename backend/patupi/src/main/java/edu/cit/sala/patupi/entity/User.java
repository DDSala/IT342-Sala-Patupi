package edu.cit.sala.patupi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class User {
    @Id
    private UUID id; // This must match Supabase Auth UUID

    @Column(unique = true, nullable = false)
    private String username;

    @Column(name = "role_id")
    private Integer roleId; // 1: Admin, 2: Barber, 3: Customer
}