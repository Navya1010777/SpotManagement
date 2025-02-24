package com.qpa.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.qpa.dto.SpotCreateDTO;
import com.qpa.dto.SpotResponseDTO;
import com.qpa.dto.SpotSearchCriteria;
import com.qpa.dto.SpotStatistics;
import com.qpa.entity.Location;
import com.qpa.entity.Spot;
import com.qpa.entity.SpotStatus;
import com.qpa.entity.SpotType;
import com.qpa.exception.ResourceNotFoundException;
import com.qpa.repository.LocationRepository;
import com.qpa.repository.SpotRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class SpotService {
	private final SpotRepository spotRepository;
	private final LocationRepository locationRepository;
	
	@Autowired
	public SpotService(SpotRepository spotRepository, LocationRepository locationRepository) {
		this.spotRepository = spotRepository;
		this.locationRepository = locationRepository;
	}
	
	public SpotResponseDTO createSpot(SpotCreateDTO spotDTO, Long ownerId) {
		Spot spot = new Spot();
		spot.setSpotNumber(spotDTO.getSpotNumber());
		spot.setSpotType(spotDTO.getSpotType());
		spot.setStatus(SpotStatus.AVAILABLE);
		spot.setHasEVCharging(spotDTO.isHasEVCharging());
		spot.setPrice(spotDTO.getPrice());
		spot.setPriceType(spotDTO.getPriceType());
		spot.setAvailableDays(spotDTO.getAvailableDays());
		spot.setSupportedVehicleTypes(spotDTO.getSupportedVehicle());
		
		List<byte[]> images = new ArrayList<>();
		for (MultipartFile file : spotDTO.getImages()) {
			try {
				images.add(file.getBytes());
			} catch (IOException e) {
				throw new RuntimeException("Failed to process image", e);
			}
		}
		spot.setSpotImages(images);
		
		Location location = new Location();
		BeanUtils.copyProperties(spotDTO.getLocation(), location);
		location = locationRepository.save(location);
		spot.setLocation(location);
		
		spot = spotRepository.save(spot);
		return convertToDTO(spot);
	}
	
	public SpotResponseDTO updateSpot(Long spotId, SpotCreateDTO spotDTO) {
		Spot spot = spotRepository.findById(spotId)
				.orElseThrow(() -> new ResourceNotFoundException("Spot not found with id : " + spotId));
		
		spot.setSpotNumber(spotDTO.getSpotNumber());
		spot.setSpotType(spotDTO.getSpotType());
		spot.setHasEVCharging(spotDTO.isHasEVCharging());
		spot.setPrice(spotDTO.getPrice());
		spot.setPriceType(spotDTO.getPriceType());
		spot.setAvailableDays(spotDTO.getAvailableDays());
		spot.setSupportedVehicleTypes(spotDTO.getSupportedVehicle());
		
		if (spotDTO.getImages() != null && !spotDTO.getImages().isEmpty()) {
			List<byte[]> images = new ArrayList<>();
			for (MultipartFile file : spotDTO.getImages()) {
				try {
					images.add(file.getBytes());
				} catch (IOException e) {
					throw new RuntimeException("Failed to process image", e);
				}
			}
			spot.setSpotImages(images);
		}
		
		Location location = spot.getLocation();
		BeanUtils.copyProperties(spotDTO.getLocation(), location);
		locationRepository.save(location);
		
		spot = spotRepository.save(spot);
		return convertToDTO(spot);
	} 
	
	public void deleteSpot(Long spotId) {
		spotRepository.deleteById(spotId);
	}
	
	public SpotResponseDTO getSpot(Long spotId) {
		Spot spot = spotRepository.findById(spotId)
				.orElseThrow(() -> new ResourceNotFoundException("Spot not found with id : " + spotId));
		return convertToDTO(spot);
	}
	
	public List<SpotResponseDTO> getAllSpots() {
	    return spotRepository.findAll().stream()
	            .map(this::convertToDTO)
	            .collect(Collectors.toList());
	}
	
	public List<SpotResponseDTO> getSpotByOwner(Long ownerId) {
		return spotRepository.findByOwnerId(ownerId).stream()
				.map(this::convertToDTO)
				.collect(Collectors.toList());
	}
	
	public List<SpotResponseDTO> searchSpots(SpotSearchCriteria criteria) {
        // Implementation for filtering spots based on various criteria
        List<Spot> spots = spotRepository.findByLocationFilters(
            criteria.getCity(),
            criteria.getArea(),
            criteria.getStreet()
        );
        
        return spots.stream()
            .filter(spot -> criteria.getSpotType() == null || spot.getSpotType() == criteria.getSpotType())
            .filter(spot -> criteria.getHasEVCharging() == null || spot.isHasEVCharging() == criteria.getHasEVCharging())
            .filter(spot -> criteria.getPriceType() == null || spot.getPriceType() == criteria.getPriceType())
            .filter(spot -> criteria.getSupportedVehicleType() == null || 
                    spot.getSupportedVehicleTypes().contains(criteria.getSupportedVehicleType()))
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
	
	public SpotResponseDTO updateSpotRating(Long spotId, Double rating) {
		Spot spot = spotRepository.findById(spotId)
				.orElseThrow(() -> new ResourceNotFoundException("Spot not found with id : " + spotId));
		spot.setRating(rating);
		spot = spotRepository.save(spot);
		return convertToDTO(spot);
	}
	
	public SpotStatistics getStatistics() {
	    List<Spot> allSpots = spotRepository.findAll();
	    
	    // Calculate total spots
	    long totalSpots = allSpots.size();
	    
	    // Calculate available and unavailable spots
	    long availableSpots = allSpots.stream()
	            .filter(spot -> spot.getStatus() == SpotStatus.AVAILABLE)
	            .count();
	    long unavailableSpots = totalSpots - availableSpots;
	    
	    // Calculate average rating
	    double averageRating = allSpots.stream()
	            .filter(spot -> spot.getRating() != null)
	            .mapToDouble(Spot::getRating)
	            .average()
	            .orElse(0.0);
	    
	    // Group spots by type
	    Map<SpotType, Long> spotsByType = allSpots.stream()
	            .collect(Collectors.groupingBy(
	                Spot::getSpotType,
	                Collectors.counting()
	            ));
	    
	    // Group spots by city
	    Map<String, Long> spotsByCity = allSpots.stream()
	            .collect(Collectors.groupingBy(
	                spot -> spot.getLocation().getCity(),
	                Collectors.counting()
	            ));
	    
	    return new SpotStatistics(
	        totalSpots,
	        availableSpots,
	        unavailableSpots,
	        averageRating,
	        spotsByType,
	        spotsByCity
	    );
	}
	
	private SpotResponseDTO convertToDTO(Spot spot) {
		SpotResponseDTO dto = new SpotResponseDTO();
		BeanUtils.copyProperties(spot, dto);
		return dto;
	}
}
