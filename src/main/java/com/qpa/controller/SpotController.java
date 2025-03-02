package com.qpa.controller;

import java.util.List;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.qpa.dto.SpotCreateDTO;
import com.qpa.dto.SpotResponseDTO;
import com.qpa.dto.SpotSearchCriteria;
import com.qpa.dto.SpotStatistics;
import com.qpa.entity.SpotStatus;
import com.qpa.entity.User;
import com.qpa.entity.VehicleType;
import com.qpa.repository.UserRepository;
import com.qpa.service.SpotService;

@RestController
@RequestMapping("/api/spots")
public class SpotController {

    private final SpotService spotService;
    
    @Autowired
    public SpotController(SpotService spotService) {
        this.spotService = spotService;
    }

    // Spot Owner endpoints
    @PostMapping("/create")
    public ResponseEntity<SpotResponseDTO> createSpot(
            @RequestPart("spot") SpotCreateDTO spotDTO,
            @RequestPart("images") List<MultipartFile> images) {
        return ResponseEntity.ok(spotService.createSpot(spotDTO, images));
    }
    
    @PutMapping("/{spotId}")
    public ResponseEntity<SpotResponseDTO> updateSpot(
            @PathVariable Long spotId,
            @RequestPart("spot") SpotCreateDTO spotDTO,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        return ResponseEntity.ok(spotService.updateSpot(spotId, spotDTO, images));
    }
    
    @DeleteMapping("/{spotId}")
    public ResponseEntity<Void> deleteSpot(@PathVariable Long spotId) {
        spotService.deleteSpot(spotId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/owner")
    public ResponseEntity<List<SpotResponseDTO>> getOwnerSpots() {
        return ResponseEntity.ok(spotService.getSpotByOwner());
    }
    
    // Vehicle Owner endpoints
    @PatchMapping("/{spotId}/rate")
    public ResponseEntity<SpotResponseDTO> rate(
            @PathVariable Long spotId,
            @RequestParam Double rating) {
        return ResponseEntity.ok(spotService.rateSpot(spotId, rating));
    }
    
    // Admin endpoints
    @GetMapping("/statistics")
    public ResponseEntity<SpotStatistics> getStatistics() {
        return ResponseEntity.ok(spotService.getStatistics());
    }
    

    // Not user specific endpoints
    @GetMapping("/all")
    public ResponseEntity<List<SpotResponseDTO>> getAllSpots() {
        return ResponseEntity.ok(spotService.getAllSpots());
    }

    @GetMapping("/search")
    public ResponseEntity<List<SpotResponseDTO>> searchSpots(SpotSearchCriteria criteria) {
        return ResponseEntity.ok(spotService.searchSpots(criteria));
    }
    
    @GetMapping("/evCharging")
    public ResponseEntity<List<SpotResponseDTO>> getSpotsByEVCharging(@RequestParam boolean hasEVCharging) {
    	
    	return new ResponseEntity<>(spotService.getSpotsByEVCharging(hasEVCharging), HttpStatus.OK);
    }
    
    @GetMapping("/availability")
    public ResponseEntity<List<SpotResponseDTO>> viewSpotAvailability(
            @RequestParam String city,
            @RequestParam VehicleType vehicleType) {
        
        return new ResponseEntity<>(spotService.getAvailableSpotsByCityAndVehicle(city, vehicleType), HttpStatus.OK);
        
    }
    
    @GetMapping("/availableSpots")
    public ResponseEntity<List<SpotResponseDTO>> viewSpotAvailability() {
    	
    	return new ResponseEntity<>(spotService.getAvailableSpots(), HttpStatus.OK);
    }
    
    
    		
    
    
}
