package com.example.demo.repository;

import com.example.demo.model.Booking;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer>{
        //Find all bookings for a SPECIFIC user (User Dashboard)
        List<Booking> findByUser(User user);

        //Find all booking by status
        List<Booking> findByStatus(String status);

        //count the pending booking exist
        long countByStatus(String status);
        
        // NEW: Add this specific query for the conflict check
        @Query("SELECT b FROM Booking b WHERE b.room.roomId = :roomId AND b.slotDate = :date")
        List<Booking> findBookingsByRoomAndDate(@Param("roomId") String roomId, @Param("date") LocalDate date);

        // Assuming Room has a field 'roomId' which is a String
        List<Booking> findByRoom_RoomIdAndSlotDate(String roomId, LocalDate date);

        // --- NEW: Fetch by Date Range ---
        // Finds all bookings for a room between a Start Date (Sunday) and End Date (Saturday)
        List<Booking> findByRoom_RoomIdAndSlotDateBetween(String roomId, LocalDate startDate, LocalDate endDate);
}
