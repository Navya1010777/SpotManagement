package com.qpa.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.qpa.dto.*;
import com.qpa.entity.*;
import com.qpa.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.qpa.exception.ResourceNotFoundException;
import com.qpa.repository.LocationRepository;
import com.qpa.repository.SpotRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class SpotService {
	private static final Logger log = LoggerFactory.getLogger(SpotService.class);
	private final SpotRepository spotRepository;
	private final LocationRepository locationRepository;
	private final UserRepository userRepository;
	
	@Autowired
	public SpotService(SpotRepository spotRepository, LocationRepository locationRepository, UserRepository userRepository) {
		this.spotRepository = spotRepository;
		this.locationRepository = locationRepository;
		this.userRepository = userRepository;
	}
	
	public SpotResponseDTO createSpot(SpotCreateDTO spotDTO, List<MultipartFile> spotImages) {
		Spot spot = new Spot();
		spot.setSpotNumber(spotDTO.getSpotNumber());
		spot.setSpotType(spotDTO.getSpotType());
		spot.setStatus(SpotStatus.AVAILABLE);
		spot.setHasEVCharging(spotDTO.isHasEVCharging());
		spot.setPrice(spotDTO.getPrice());
		spot.setPriceType(spotDTO.getPriceType());
		spot.setSupportedVehicleTypes(spotDTO.getSupportedVehicle());
		spot.setOwner(userRepository.findById(getCurrentUserId()).get()); // might throw exception, needs to be handled

		List<byte[]> images = new ArrayList<>();
		for (MultipartFile file : spotImages) {
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
	
	public SpotResponseDTO updateSpot(Long spotId, SpotCreateDTO spotDTO, List<MultipartFile> spotImages) {
		Spot spot = spotRepository.findById(spotId)
				.orElseThrow(() -> new ResourceNotFoundException("Spot not found with id : " + spotId));
		
		spot.setSpotNumber(spotDTO.getSpotNumber());
		spot.setSpotType(spotDTO.getSpotType());
		spot.setHasEVCharging(spotDTO.isHasEVCharging());
		spot.setPrice(spotDTO.getPrice());
		spot.setPriceType(spotDTO.getPriceType());
		spot.setSupportedVehicleTypes(spotDTO.getSupportedVehicle());
		
		if (spotDTO.getImages() != null && !spotDTO.getImages().isEmpty()) {
			List<byte[]> images = new ArrayList<>();
			for (MultipartFile file : spotImages) {
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
	
	public List<SpotResponseDTO> getSpotByOwner() {
		return spotRepository.findByOwnerId(getCurrentUserId()).stream()
				.map(this::convertToDTO)
				.collect(Collectors.toList());
	}

	// Needs some work, not efficient right now.
	public List<SpotResponseDTO> searchSpots(SpotSearchCriteria criteria) {
        // Implementation for filtering spots based on various criteria
        List<Spot> spots = spotRepository.findByLocationFilters(
            criteria.getCity()
        );

        return spots.stream()
            .filter(spot -> criteria.getSpotType() == null || spot.getSpotType() == criteria.getSpotType())
            .filter(spot -> criteria.getHasEVCharging() == null || spot.hasEVCharging() == criteria.getHasEVCharging())
            .filter(spot -> criteria.getPriceType() == null || spot.getPriceType() == criteria.getPriceType())
            .filter(spot -> criteria.getSupportedVehicleType() == null ||
                    spot.getSupportedVehicleTypes().contains(criteria.getSupportedVehicleType()))
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

	public SpotResponseDTO rateSpot(Long spotId, Double rating) {
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
	    
	    return new SpotStatistics(
	        totalSpots,
	        availableSpots,
	        unavailableSpots
	    );
	}

	private SpotResponseDTO convertToDTO(Spot spot) {
		SpotResponseDTO dto = new SpotResponseDTO();
		BeanUtils.copyProperties(spot, dto);

		if (spot.getLocation() != null) {
			LocationDTO locationDTO = new LocationDTO();
			BeanUtils.copyProperties(spot.getLocation(), locationDTO);
			dto.setLocation(locationDTO);
		}

		return dto;
	}

	private Long getCurrentUserId() {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return userRepository.findByUsername(userDetails.getUsername())
				.map(User::getId)
				.orElseThrow(() -> new RuntimeException("User not found"));
	}
	
	
	public List<SpotResponseDTO> getSpotsByEVCharging(boolean hasEVCharging) {
	    List<Spot> spots = spotRepository.findByHasEVCharging(hasEVCharging);
	    return spots.stream().map(this::convertToDTO).collect(Collectors.toList());
	}
	
	public List<SpotResponseDTO> getAvailableSpotsByCityAndVehicle(String city, VehicleType vehicleType) {
        List<Spot> spots = spotRepository.findAvailableSpotsByCityAndVehicleType(city, vehicleType);
        return spots.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
	
	public List<SpotResponseDTO> getAvailableSpots() {
		List<Spot> spots = spotRepository.findByStatus(SpotStatus.AVAILABLE);
		return spots.stream()
				.map(this::convertToDTO)
				.collect(Collectors.toList());
	}


}
