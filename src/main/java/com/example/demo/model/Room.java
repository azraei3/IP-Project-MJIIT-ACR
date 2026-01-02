package com.example.demo.model;
import jakarta.persistence.*;

@Entity
@Table(name = "rooms")
public class Room {
    @Id
    @Column(name = "room_id", length = 20, nullable = false)
    private String roomId;

    @Column(name = "name", length=120, nullable = false)
    private String name;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Column(name = "floor", length = 10)
    private String floor;

    @Column(name = "active")
    private boolean active = true;

    public Room(){}

    public Room(String roomId, String name, int capacity, String floor){
        this.roomId = roomId;
        this.name = name;
        this.capacity = capacity;
        this.floor = floor;
        this.active = true;
    }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getFloor() { return floor; }
    public void setFloor(String floor) { this.floor = floor; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
