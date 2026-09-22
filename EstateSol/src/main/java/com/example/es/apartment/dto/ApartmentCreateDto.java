package com.example.es.apartment.dto;

public class ApartmentCreateDto {
	
	private String area;
	private int squareMeters;
	private int floor;
	private String googleMapsPin;
	private String region;
	private java.math.BigDecimal budget;
	
	public String getArea() {
	    return area;
	}

	public void setArea(String area) {
	    this.area = area;
	}
	
	public int getSquareMeters() {
		return squareMeters;
	}
	public void setSquareMeters(int squareMeters) {
		this.squareMeters = squareMeters;
	}
	public int getFloor() {
		return floor;
	}
	public void setFloor(int floor) {
		this.floor = floor;
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
}
