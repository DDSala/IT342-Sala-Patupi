package edu.cit.sala.patupi.repository;

import edu.cit.sala.patupi.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("SELECT a FROM Appointment a WHERE DATE(a.scheduled_at) = :date AND a.status = 'CONFIRMED'")
    List<Appointment> findBookedSlotsByDate(@Param("date") String date);


    @Query("SELECT a FROM Appointment a WHERE a.user.userId = :customerId ORDER BY a.scheduled_at DESC")
    List<Appointment> findByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.user.userId = :customerId AND a.status NOT IN ('FINISHED', 'CANCELLED')")
    boolean hasActiveAppointment(@Param("customerId") Long customerId);

    @Modifying
    @Transactional
    @Query("UPDATE Appointment a SET a.status = 'CANCELLED' WHERE a.id = :id AND a.user.userId = :customerId")
    int cancelAppointment(@Param("id") Long id, @Param("customerId") Long customerId);
}