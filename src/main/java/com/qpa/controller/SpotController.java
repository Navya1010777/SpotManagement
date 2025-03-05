package com.qpa.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.qpa.dto.SpotCreateDTO;
import com.qpa.dto.SpotResponseDTO;
import com.qpa.dto.SpotSearchCriteria;
import com.qpa.dto.SpotStatistics;
import com.qpa.entity.SpotStatus;
import com.qpa.entity.VehicleType;
import com.qpa.service.SpotService;

@RestController
@RequestMapping("/api/spots")
public class SpotController {

    private final SpotService spotService;

    @Autowired
    public SpotController(SpotService spotService) {
        this.spotService = spotService;
    }

    @PostMapping("/create")
    public ResponseEntity<SpotResponseDTO> createSpot(
            @RequestPart("spot") SpotCreateDTO spotDTO,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestParam Long userId) {
        List<MultipartFile> imageList = (images != null) ? images : Collections.emptyList();
        return ResponseEntity.ok(spotService.createSpot(spotDTO, images, userId));
    }

    @PutMapping("/{spotId}")
    public ResponseEntity<SpotResponseDTO> updateSpot(
            @PathVariable Long spotId,
            @RequestPart("spot") SpotCreateDTO spotDTO,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestParam Long userId) {
        // If needed, you can add a check to ensure the user owns the spot
        return ResponseEntity.ok(spotService.updateSpot(spotId, spotDTO, images));
    }

    @DeleteMapping("/{spotId}")
    public ResponseEntity<Void> deleteSpot(
            @PathVariable Long spotId,
            @RequestParam Long userId) {
        // Add ownership check if needed
        spotService.deleteSpot(spotId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/owner")
    public ResponseEntity<List<SpotResponseDTO>> getOwnerSpots(
            @RequestParam Long userId) {
        return ResponseEntity.ok(spotService.getSpotByOwner(userId));
    }

    // Vehicle Owner endpoints
    @PatchMapping("/{spotId}/rate")
    public ResponseEntity<SpotResponseDTO> rate(
            @PathVariable Long spotId,
            @RequestParam Double rating,
            @RequestParam Long userId) {
        // You might want to add logic to prevent rating your own spot
        return ResponseEntity.ok(spotService.rateSpot(spotId, rating));
    }

    // Remaining endpoints stay mostly the same
    @GetMapping("/statistics")
    public ResponseEntity<SpotStatistics> getStatistics() {
        return ResponseEntity.ok(spotService.getStatistics());
    }

    @GetMapping("/all")
    public ResponseEntity<List<SpotResponseDTO>> getAllSpots() {
        return ResponseEntity.ok(spotService.getAllSpots());
    }

    @GetMapping("/search")
    public ResponseEntity<List<SpotResponseDTO>> searchSpots(SpotSearchCriteria criteria) {
        return ResponseEntity.ok(spotService.searchSpots(criteria));
    }


    // All these 3 methods are redundant since searching gives us all of this

    // this method is redundant since in search we are giving ev charging boolean
    @GetMapping("/evCharging")
    public ResponseEntity<List<SpotResponseDTO>> getSpotsByEVCharging(
            @RequestParam boolean hasEVCharging) {
        return new ResponseEntity<>(spotService.getSpotsByEVCharging(hasEVCharging), HttpStatus.OK);
    }

    // this method is also redundant since we are also giving availability while searching
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