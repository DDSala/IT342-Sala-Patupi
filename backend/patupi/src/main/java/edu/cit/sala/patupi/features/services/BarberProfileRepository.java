package edu.cit.sala.patupi.features.services;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BarberProfileRepository extends JpaRepository<BarberProfile, Long> {

    List<BarberProfile> findByStatus(String status);
}