package edu.cit.sala.patupi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.cit.sala.patupi.entity.BarberProfile;

@Repository
public interface BarberProfileRepository extends JpaRepository<BarberProfile, Long> {

    
    List<BarberProfile> findByStatus(String status);


}