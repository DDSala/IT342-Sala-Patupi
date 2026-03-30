package edu.cit.sala.patupi.service;

import edu.cit.sala.patupi.entity.Appointment;
import edu.cit.sala.patupi.repository.AppointmentRepository;
import edu.cit.sala.patupi.repository.ServiceRepository; // Assuming you have this
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    public List<Map<String, Object>> getCustomerAppointmentsWithNames(Long customerId) {
        List<Appointment> appointments = appointmentRepository.findByCustomerId(customerId);
        List<Map<String, Object>> enrichedList = new ArrayList<>();

        for (Appointment app : appointments) {
            Map<String, Object> map = new HashMap<>();
            map.put("appointmentId", app.getId());
            map.put("status", app.getStatus());
            map.put("scheduledAt", app.getScheduled_at());
            map.put("description", app.getDescription());
            map.put("photo", app.getReference_photo());

if (app.getService_id() != 0) {
    String name = serviceRepository.findById(app.getService_id())
            .map(s -> s.getName())
            .orElse("Standard Service");
    map.put("serviceName", name);
} else {
    map.put("serviceName", "Pending Selection");
}

            enrichedList.add(map);
        }
        return enrichedList;
    }
}