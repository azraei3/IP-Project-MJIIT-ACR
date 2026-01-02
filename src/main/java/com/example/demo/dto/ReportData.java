package com.example.demo.dto;

public class ReportData {
    private String label; // e.g., Room Name or Date
    private Long value;   // e.g., Count of bookings

    public ReportData(String label, Long value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() { return label; }
    public Long getValue() { return value; }
}