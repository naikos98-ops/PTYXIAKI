package com.example.es.apartment.dto;

public class ApartmentUserUpdateDto {
	
	private String area;
    private Integer squareMeters;
    private Integer floor;
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

}
