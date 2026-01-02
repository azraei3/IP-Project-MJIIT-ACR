package com.example.demo.service;

import com.example.demo.model.Room;
import com.example.demo.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    // Get all rooms
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    // Save or Update a room (Safe Version)
    public void saveRoom(Room room) {
        // Only save if the room object actually exists
        if (room != null) {
            roomRepository.save(room);
        }
    }

    // Delete a room (Safe Version)
    public void deleteRoom(String roomId) {
        // Only delete if the ID string is not null
        if (roomId != null) {
            roomRepository.deleteById(roomId);
        }
    }
    
    // Count rooms (for the Dashboard)
    public long countRooms() {
        return roomRepository.count();
    }

    public Room getRoomById(String roomId) {
        // Returns the room if found, or null if not found
        return roomRepository.findById(roomId).orElse(null);
    }
}