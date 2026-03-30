package edu.cit.sala.patupi.repository;

import edu.cit.sala.patupi.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    

    @Query("SELECT a FROM Appointment a WHERE DATE(a.scheduled_at) = :date AND a.status = 'CONFIRMED'")
    List<Appointment> findBookedSlotsByDate(@Param("date") String date);


    @Query(value = "SELECT * FROM appointments WHERE customer_id = :customerId ORDER BY scheduled_at DESC", nativeQuery = true)
    List<Appointment> findByCustomerId(@Param("customerId") Long customerId);
    
}