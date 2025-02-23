package com.qpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.qpa.entity.PriceType;
import com.qpa.entity.Spot;
import com.qpa.entity.SpotStatus;
import com.qpa.entity.SpotType;

@Repository
public interface SpotRepository extends JpaRepository<Spot, Long> {
	List<Spot> findByStatus(SpotStatus status);
	List<Spot> findBySpotType(SpotType spotType);
	List<Spot> findByHasEVCharging(boolean hasEVCharging);
	List<Spot> findByPriceType(PriceType priceType);
	List<Spot> findByLocation_City(String city);
	List<Spot> findByLocation_Area(String area);
	List<Spot> findByOwnerId(Long ownerId);
	
	@Query("SELECT s FROM Spot s WHERE s.location.city = :city OR s.location.area = :area OR s.location.streetAddress LIKE %:street%")
	List<Spot> findByLocationFilters(String city, String area, String street);
}
