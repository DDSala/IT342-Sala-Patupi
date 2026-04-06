package edu.cit.sala.patupi.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "appointments"}) 
    private User user;

    @Column(name = "barber_id")
    @JsonProperty("barberId")
    private Long barberId;

    @Column(name = "service_id")
    @JsonProperty("serviceId")
    private Integer service_id;

    @Column(name = "status")
    private String status = "PENDING";

    @Column(name = "description")
    private String description;

    @Column(name = "payment_method")
    private String payment_method = "CASH_ONSITE";

    @Column(name = "total_amount")
    @JsonProperty("totalAmount")
    private BigDecimal total_amount;

    @Column(name = "scheduled_at")
    @JsonProperty("scheduledAt")
    private LocalDateTime scheduled_at;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime created_at = LocalDateTime.now();

    // Helpers for the Admin Dashboard to show names without extra API calls
    @JsonProperty("customerName")
    public String getCustomerName() {
        return user != null ? user.getFullName() : "Unknown";
    }

    @JsonProperty("customerId")
    public Long getCustomerId() {
        return user != null ? user.getUserId() : null;
    }

    // Standard Getters/Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Long getBarberId() { return barberId; }
    public void setBarberId(Long barberId) { this.barberId = barberId; }
    public Integer getService_id() { return service_id; }
    public void setService_id(Integer service_id) { this.service_id = service_id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPayment_method() { return payment_method; }
    public void setPayment_method(String payment_method) { this.payment_method = payment_method; }
    public BigDecimal getTotal_amount() { return total_amount; }
    public void setTotal_amount(BigDecimal total_amount) { this.total_amount = total_amount; }
    public LocalDateTime getScheduled_at() { return scheduled_at; }
    public void setScheduled_at(LocalDateTime scheduled_at) { this.scheduled_at = scheduled_at; }
}