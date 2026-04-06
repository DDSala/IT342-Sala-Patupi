package edu.cit.sala.patupi.dto;

import java.time.LocalDateTime;

public class AppointmentResponseDTO {
    
    private Long appointmentId;
    private String customerName;
    private Long barberId;
    private String barberName;
    private String service;
    private String description;
    private LocalDateTime scheduledAt;
    private String status;
    private String referencePhoto; 

    public AppointmentResponseDTO() {}

    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public Long getBarberId() { return barberId; }
    public void setBarberId(Long barberId) { this.barberId = barberId; }

    public String getBarberName() { return barberName; }
    public void setBarberName(String barberName) { this.barberName = barberName; }

    public String getService() { return service; }
    public void setService(String service) { this.service = service; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReferencePhoto() { return referencePhoto; }
    public void setReferencePhoto(String referencePhoto) { this.referencePhoto = referencePhoto; }
} 

