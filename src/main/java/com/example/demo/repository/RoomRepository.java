package com.example.demo.repository;

import com.example.demo.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Note: The ID type is 'String', not 'Integer'
@Repository
public interface RoomRepository extends JpaRepository<Room, String> {
    // We can add custom queries here later if needed
}