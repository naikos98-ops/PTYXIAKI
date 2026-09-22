package com.example.es.apartment.dto;

import com.example.es.apartment.entity.ProjectStatus;
import java.math.BigDecimal;

public class LeadPoolDto {
    private Long id;
    private String region;
    private String area;
    private int squareMeters;
    private int floor;
    private String estimatedCost;
    private BigDecimal budget;
    private ProjectStatus status;
    private boolean isUnlocked;
    private String imagePath;
    private String renovatedImagePath;
    private String googleMapsPin;
    private String ownerName;

    public LeadPoolDto() {}

    public LeadPoolDto(Long id, String region, String area, int squareMeters, int floor,
                       String estimatedCost, BigDecimal budget, ProjectStatus status,
                       boolean isUnlocked, String googleMapsPin, String ownerName,
                       String imagePath, String renovatedImagePath) {
        this.id = id;
        this.region = region;
        this.area = area;
        this.squareMeters = squareMeters;
        this.floor = floor;
        this.estimatedCost = estimatedCost;
        this.budget = budget;
        this.status = status;
        this.isUnlocked = isUnlocked;
        this.googleMapsPin = isUnlocked ? googleMapsPin : "[MASKED - APPROVE APPOINTMENT TO VIEW]";
        this.ownerName = isUnlocked ? ownerName : "[MASKED - ANONYMOUS OWNER]";
        this.imagePath = imagePath;
        this.renovatedImagePath = renovatedImagePath;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public int getSquareMeters() { return squareMeters; }
    public void setSquareMeters(int squareMeters) { this.squareMeters = squareMeters; }
    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }
    public String getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(String estimatedCost) { this.estimatedCost = estimatedCost; }
    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }
    public ProjectStatus getStatus() { return status; }
    public void setStatus(ProjectStatus status) { this.status = status; }
    public boolean isUnlocked() { return isUnlocked; }
    public void setUnlocked(boolean unlocked) { isUnlocked = unlocked; }
    public String getGoogleMapsPin() { return googleMapsPin; }
    public void setGoogleMapsPin(String googleMapsPin) { this.googleMapsPin = googleMapsPin; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public String getRenovatedImagePath() { return renovatedImagePath; }
    public void setRenovatedImagePath(String renovatedImagePath) { this.renovatedImagePath = renovatedImagePath; }
}
