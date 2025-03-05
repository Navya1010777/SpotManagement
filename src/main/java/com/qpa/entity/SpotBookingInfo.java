
package com.qpa.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "spot_booking_info") // Explicitly defining the table name
public class SpotBookingInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;
    
    @NotBlank
    private LocalDate bookingDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL) // ✅ Cascade ALL
    @JoinColumn(name = "spot_id")
    private Spot spotInfo;
    
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL) // ✅ Cascade ALL
    @JoinColumn(name = "vehicle_id")
    private VehicleInfo vehicle;

//    @OneToMany(mappedBy = "spotBookingInfo", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<AddOns> addOns;

    // Default constructor
    public SpotBookingInfo() {
    }

    // Parameterized constructor
    public SpotBookingInfo(Long bookingId, LocalDate bookingDate, LocalDate startDate, LocalDate endDate,
                           LocalDateTime startTime, LocalDateTime endTime, String status, Spot spotInfo,
                           VehicleInfo vehicle) {
        this.bookingId = bookingId;
        this.bookingDate = bookingDate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.spotInfo = spotInfo;
        this.vehicle = vehicle;
 //       this.addOns = addOns;
    }

    // Getters and Setters
    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

	public Spot getSpotInfo() {
		return spotInfo;
	}

	public void setSpotInfo(Spot spotInfo) {
		this.spotInfo = spotInfo;
	}

	public VehicleInfo getVehicle() {
		return vehicle;
	}

	public void setVehicle(VehicleInfo vehicle) {
		this.vehicle = vehicle;
	}


}
