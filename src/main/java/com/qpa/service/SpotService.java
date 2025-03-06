package com.qpa.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.qpa.dto.*;
import com.qpa.entity.*;
import com.qpa.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.qpa.exception.InvalidEntityException;
import com.qpa.exception.ResourceNotFoundException;
import com.qpa.repository.LocationRepository;
import com.qpa.repository.SpotRepository;
import com.qpa.repository.*;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class SpotService {
	private final SpotRepository spotRepository;
	private final LocationRepository locationRepository;
	private final UserRepository userRepository;
	private final SpotBookingInfoRepository bookingRepository;
	
	@Autowired
	public SpotService(SpotRepository spotRepository, LocationRepository locationRepository, UserRepository userRepository, SpotBookingInfoRepository bookingRepository) {
		this.spotRepository = spotRepository;
		this.locationRepository = locationRepository;
		this.userRepository = userRepository;
		this.bookingRepository = bookingRepository;
	}
	
	public SpotResponseDTO createSpot(SpotCreateDTO spotDTO, List<MultipartFile> spotImages, Long userId) {
		Spot spot = new Spot();
		spot.setSpotNumber(spotDTO.getSpotNumber());
		spot.setSpotType(spotDTO.getSpotType());
		spot.setStatus(SpotStatus.AVAILABLE);
		spot.setHasEVCharging(spotDTO.getHasEVCharging());
		spot.setPrice(spotDTO.getPrice());
		spot.setPriceType(spotDTO.getPriceType());
		spot.setSupportedVehicleTypes(spotDTO.getSupportedVehicle());
		spot.setOwner(userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId)));

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
	
	public SpotResponseDTO updateSpot(Long spotId, SpotCreateDTO spotDTO, List<MultipartFile> spotImages) throws InvalidEntityException{
		Spot spot = spotRepository.findById(spotId)
				.orElseThrow(() -> new InvalidEntityException("Spot not found with id : " + spotId));
		
		spot.setSpotNumber(spotDTO.getSpotNumber());
		spot.setSpotType(spotDTO.getSpotType());
		spot.setHasEVCharging(spotDTO.getHasEVCharging());
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

	public List<SpotResponseDTO> getSpotByOwner(Long userId) {
		return spotRepository.findByOwnerId(userId).stream()
				.map(this::convertToDTO)
				.collect(Collectors.toList());
	}

	// Needs some work, not efficient right now.
	public List<SpotResponseDTO> searchSpots(SpotSearchCriteria criteria) {
        // Implementation for filtering spots based on various criteria
        List<Spot> spots = spotRepository.findByLocationFilters(
            criteria.getCity()
        );

        List<SpotResponseDTO> filteredSpots =  spots.stream()
            .filter(spot -> criteria.getSpotType() == null || spot.getSpotType() == criteria.getSpotType())
            .filter(spot -> criteria.getHasEVCharging() == null || spot.getHasEVCharging() == criteria.getHasEVCharging())
            .filter(spot -> criteria.getPriceType() == null || spot.getPriceType() == criteria.getPriceType())
            .filter(spot -> criteria.getSupportedVehicleType() == null ||
                    spot.getSupportedVehicleTypes().contains(criteria.getSupportedVehicleType()))
			.filter(spot -> criteria.getStatus() == null || spot.getStatus() == criteria.getStatus())
            .map(this::convertToDTO)
            .collect(Collectors.toList());

		return filteredSpots;
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
	
	public SpotResponseDTO getSpotByBookingId(long bookingId) throws InvalidEntityException{
        Spot spot = bookingRepository.findSpotByBookingId(bookingId);
        if (spot == null) {
            throw new InvalidEntityException("No spot found for booking ID: " + bookingId);
        }
        return convertToDTO(spot);
    }
	
	public List<SpotResponseDTO> getBookedSpots() {
        List<Spot> bookedSpots = bookingRepository.findBookedSpots();
        if (bookedSpots.isEmpty()) {
            throw new RuntimeException("No booked spots found.");
        }
        return bookedSpots.stream()
				.map(this::convertToDTO)
				.collect(Collectors.toList());
    }
	
	public List<SpotResponseDTO> getAvailableSpotsByStartAndEndDate(LocalDate startDate, LocalDate endDate) {
        List<Spot> bookedSpots = bookingRepository.findSpotsByStartAndEndDate(startDate, endDate);
        if (bookedSpots.isEmpty()) {
            throw new RuntimeException("No booked spots found between "+ startDate + " and " + endDate);
        }
        return bookedSpots.stream()
				.map(this::convertToDTO)
				.collect(Collectors.toList());
    }


}
