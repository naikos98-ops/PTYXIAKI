package com.example.es.apartment.dto;

public class ApartmentUpdateDto {

    private String area;
    private Integer squareMeters;
    private Integer floor;
    private Double estimatedPrice;
    private Double estimatedRent;
    private String estimatedCost;
    private String googleMapsPin;
    private String region;
    private java.math.BigDecimal budget;
    private com.example.es.apartment.entity.ProjectStatus status;

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public Integer getSquareMeters() {
        return squareMeters;
    }

    public void setSquareMeters(Integer squareMeters) {
        this.squareMeters = squareMeters;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }


    public Double getEstimatedPrice() {
        return estimatedPrice;
    }

    public void setEstimatedPrice(Double estimatedPrice) {
        this.estimatedPrice = estimatedPrice;
    }

    public Double getEstimatedRent() {
        return estimatedRent;
    }

    public void setEstimatedRent(Double estimatedRent) {
        this.estimatedRent = estimatedRent;
    }

    public String getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(String estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public String getGoogleMapsPin() {
        return googleMapsPin;
    }

    public void setGoogleMapsPin(String googleMapsPin) {
        this.googleMapsPin = googleMapsPin;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public java.math.BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(java.math.BigDecimal budget) {
        this.budget = budget;
    }

    public com.example.es.apartment.entity.ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(com.example.es.apartment.entity.ProjectStatus status) {
        this.status = status;
    }
}