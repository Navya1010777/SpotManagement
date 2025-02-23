package com.qpa.entity;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "spots")
public class Spot {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long spotId;
	
	private String spotNumber;
	
	@ManyToOne
	@JoinColumn(name = "owner_id", nullable = false)
	private User owner;
	
	@Enumerated(EnumType.STRING)
	private SpotType spotType;
	
	@Enumerated(EnumType.STRING)
	private SpotStatus status;
	
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "location_id", nullable = false)
	private Location location;
	
	private boolean hasEVCharging;
	
	private double price;
	
	@Enumerated(EnumType.STRING)
    private PriceType priceType;
    
    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "spot_available_days", joinColumns = @JoinColumn(name = "spot_id"))
    @Column(name = "day_of_week")
    private List<DayOfWeek> availableDays;
    
    private Double rating;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @Lob
    @ElementCollection
    @CollectionTable(name = "spot_images", joinColumns = @JoinColumn(name = "spot_id"))
    @Column(name = "image_data", columnDefinition = "BOLB")
    private List<byte[]> spotImages;
    
    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "spot_vehicle_types", joinColumns = @JoinColumn(name = "spot_id"))
    @Column(name = "vehicle_type")
    private Set<VehicleType> supportedVehicleTypes;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

	public Spot() {

	}

	public Spot(Long spotId, String spotNumber, SpotType spotType, SpotStatus status, Location location,
			boolean hasEVCharging, double price, PriceType priceType, List<DayOfWeek> availableDays, Double rating,
			LocalDateTime createdAt, LocalDateTime updatedAt, List<byte[]> spotImages,
			Set<VehicleType> supportedVehicleTypes) {
		super();
		this.spotId = spotId;
		this.spotNumber = spotNumber;
		this.spotType = spotType;
		this.status = status;
		this.location = location;
		this.hasEVCharging = hasEVCharging;
		this.price = price;
		this.priceType = priceType;
		this.availableDays = availableDays;
		this.rating = rating;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.spotImages = spotImages;
		this.supportedVehicleTypes = supportedVehicleTypes;
	}

	public Long getSpotId() {
		return spotId;
	}

	public void setSpotId(Long spotId) {
		this.spotId = spotId;
	}

	public String getSpotNumber() {
		return spotNumber;
	}

	public void setSpotNumber(String spotNumber) {
		this.spotNumber = spotNumber;
	}

	public SpotType getSpotType() {
		return spotType;
	}

	public void setSpotType(SpotType spotType) {
		this.spotType = spotType;
	}

	public SpotStatus getStatus() {
		return status;
	}

	public void setStatus(SpotStatus status) {
		this.status = status;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public boolean isHasEVCharging() {
		return hasEVCharging;
	}

	public void setHasEVCharging(boolean hasEVCharging) {
		this.hasEVCharging = hasEVCharging;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public PriceType getPriceType() {
		return priceType;
	}

	public void setPriceType(PriceType priceType) {
		this.priceType = priceType;
	}

	public List<DayOfWeek> getAvailableDays() {
		return availableDays;
	}

	public void setAvailableDays(List<DayOfWeek> availableDays) {
		this.availableDays = availableDays;
	}

	public Double getRating() {
		return rating;
	}

	public void setRating(Double rating) {
		this.rating = rating;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	
	public List<byte[]> getSpotImages() {
		return spotImages;
	}

	public void setSpotImages(List<byte[]> spotImages) {
		this.spotImages = spotImages;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Set<VehicleType> getSupportedVehicleTypes() {
		return supportedVehicleTypes;
	}

	public void setSupportedVehicleTypes(Set<VehicleType> supportedVehicleTypes) {
		this.supportedVehicleTypes = supportedVehicleTypes;
	}
    
    
}
