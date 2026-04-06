package edu.cit.sala.patupi.service;

import edu.cit.sala.patupi.dto.AppointmentResponseDTO;
import edu.cit.sala.patupi.entity.Appointment;
import edu.cit.sala.patupi.repository.AppointmentRepository;
import edu.cit.sala.patupi.repository.ServiceRepository;
import edu.cit.sala.patupi.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    
    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private UserRepository userRepository;


    public List<Map<String, Object>> getCustomerAppointmentsWithNames(Long customerId) {
        List<Appointment> appointments = appointmentRepository.findByCustomerId(customerId);
        List<Map<String, Object>> enrichedList = new ArrayList<>();

        for (Appointment app : appointments) {
            Map<String, Object> map = new HashMap<>();
            map.put("appointmentId", app.getId()); 
            map.put("status", app.getStatus());
            map.put("scheduledAt", app.getScheduled_at());
            map.put("description", app.getDescription());

            if (app.getService_id() != null && app.getService_id() != 0) {
                serviceRepository.findById(app.getService_id()).ifPresent(s -> {
                    map.put("serviceName", s.getName());
                    map.put("totalAmount", s.getBase_price()); 
                });
            } else {
                map.put("serviceName", "Pending Selection");
                map.put("totalAmount", 0.00);
            }

            enrichedList.add(map);
        }
        return enrichedList;
    }


    public boolean isCustomerBusy(Long customerId) {
        return appointmentRepository.hasActiveAppointment(customerId);
    }


    public List<AppointmentResponseDTO> getAllAppointmentsForAdmin() {
        List<Appointment> appointments = appointmentRepository.findAll();
        return appointments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    private AppointmentResponseDTO convertToDTO(Appointment app) {
        AppointmentResponseDTO dto = new AppointmentResponseDTO();
        
        dto.setAppointmentId(app.getId()); 
        dto.setStatus(app.getStatus());
        dto.setScheduledAt(app.getScheduled_at());
        dto.setDescription(app.getDescription());


        if (app.getUser() != null) {
            dto.setCustomerName(app.getUser().getFullName());
        } else {
            dto.setCustomerName("Unknown Customer");
        }

    
        if (app.getBarberId() != null) {
            userRepository.findById(app.getBarberId())
                .ifPresent(b -> dto.setBarberName(b.getFullName()));
        }

     
        if (app.getService_id() != null && app.getService_id() != 0) {
            serviceRepository.findById(app.getService_id())
                .ifPresent(s -> dto.setService(s.getName()));
        } else {
            dto.setService("Not Selected");
        }

        return dto;
    }


}