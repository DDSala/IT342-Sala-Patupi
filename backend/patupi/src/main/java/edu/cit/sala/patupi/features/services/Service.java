package edu.cit.sala.patupi.features.services;

import com.fasterxml.jackson.annotation.JsonProperty; 
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "services")
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_id")
    @JsonProperty("service_id")
    private Integer service_id;

    @Column(name = "name", nullable = false)
    @JsonProperty("name")
    private String name;

    @Column(name = "base_price")
    @JsonProperty("base_price")
    private BigDecimal base_price;

    @Column(name = "duration_minutes")
    @JsonProperty("duration_minutes")
    private Integer duration_minutes;

    public Integer getService_id() { return service_id; }
    public void setService_id(Integer service_id) { this.service_id = service_id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getBase_price() { return base_price; }
    public void setBase_price(BigDecimal base_price) { this.base_price = base_price; }
    
    public Integer getDuration_minutes() { return duration_minutes; }
    public void setDuration_minutes(Integer duration_minutes) { this.duration_minutes = duration_minutes; }
}