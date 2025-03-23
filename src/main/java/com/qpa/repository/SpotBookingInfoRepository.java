package com.qpa.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.qpa.entity.Spot;
import com.qpa.entity.SpotBookingInfo;

public interface SpotBookingInfoRepository extends JpaRepository<SpotBookingInfo, Integer> {

    @Query("SELECT s.spotInfo FROM SpotBookingInfo s WHERE s.bookingId = :bookingId")
    Spot findSpotByBookingId(@Param("bookingId") long bookingId);
    
    @Query("SELECT s.spotInfo FROM SpotBookingInfo s WHERE s.status = 'CONFIRMED'")
    List<Spot> findBookedSpots();

    @Query("SELECT s.spotInfo FROM SpotBookingInfo s " +
            "WHERE :startDate <= s.endDate AND :endDate >= s.startDate " +
            "AND s.status = 'CONFIRMED'")
    List<Spot> findSpotsByStartAndEndDate(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

}
