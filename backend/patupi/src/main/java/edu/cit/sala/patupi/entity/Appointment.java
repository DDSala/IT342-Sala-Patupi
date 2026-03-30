package edu.cit.sala.patupi.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customer_id;

    @Column(name = "service_id")
    private Integer service_id;

    @Column(name = "status")
    private String status = "PENDING";

    @Column(name = "reference_photo")
    private String reference_photo;

    @Column(name = "description")
    private String description;

    @Column(name = "payment_method")
    private String payment_method = "CASH_ONSITE";

    @Column(name = "total_amount")
    private BigDecimal total_amount;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduled_at;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime created_at = LocalDateTime.now();


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCustomer_id() { return customer_id; }
    public void setCustomer_id(Long customer_id) { this.customer_id = customer_id; }
    public Integer getService_id() { return service_id; }
    public void setService_id(Integer service_id) { this.service_id = service_id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReference_photo() { return reference_photo; }
    public void setReference_photo(String reference_photo) { this.reference_photo = reference_photo; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPayment_method() { return payment_method; }
    public void setPayment_method(String payment_method) { this.payment_method = payment_method; }
    public BigDecimal getTotal_amount() { return total_amount; }
    public void setTotal_amount(BigDecimal total_amount) { this.total_amount = total_amount; }
    public LocalDateTime getScheduled_at() { return scheduled_at; }
    public void setScheduled_at(LocalDateTime scheduled_at) { this.scheduled_at = scheduled_at; }
}