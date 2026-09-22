package com.example.es.apartment.entity;

import com.example.es.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import java.math.BigDecimal;

@Entity
public class Apartment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User owner;
	private String renovatedImagePath;
	private String estimatedCost;

	@Column(nullable = false)
	private Boolean aiProcessed = false;
	@Column(length = 1000)
	private String aiProcessingError;

	public String getEstimatedCost() {
		return estimatedCost;
	}

	public void setEstimatedCost(String estimatedCost) {
		this.estimatedCost = estimatedCost;
	}

	public String getRenovatedImagePath() {
		return renovatedImagePath;
	}

	public void setRenovatedImagePath(String renovatedImagePath) {
		this.renovatedImagePath = renovatedImagePath;
	}

	public boolean isAiProcessed() {
		return aiProcessed;
	}

	public void setAiProcessed(boolean aiProcessed) {
		this.aiProcessed = aiProcessed;
	}

	public String getAiProcessingError() {
		return aiProcessingError;
	}

	public void setAiProcessingError(String aiProcessingError) {
		this.aiProcessingError = aiProcessingError;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setEstimatedPrice(Double estimatedPrice) {
		this.estimatedPrice = estimatedPrice;
	}

	public void setEstimatedRent(Double estimatedRent) {
		this.estimatedRent = estimatedRent;
	}

	private String area;
	private int squareMeters;
	private int floor;
	@Column(nullable = true)
	private Double estimatedPrice;

	@Column(nullable = true)
	private Double estimatedRent;

	private String imagePath;
	public Apartment() {
	}
	public Long getId() {
		return id;
	}

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

	public Double getEstimatedPrice() {
		return estimatedPrice;
	}

	public void setEstimatedPrice(double estimatedPrice) {
		this.estimatedPrice = estimatedPrice;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public Double getEstimatedRent() {
		return estimatedRent;
	}

	public void setEstimatedRent(double estimatedRent) {
		this.estimatedRent = estimatedRent;
	}

	private String googleMapsPin;
	private String region;

	@Column(nullable = true)
	private BigDecimal budget;

	@Enumerated(EnumType.STRING)
	@Column(nullable = true)
	private ProjectStatus status = ProjectStatus.DRAFT;

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

	public BigDecimal getBudget() {
		return budget;
	}

	public void setBudget(BigDecimal budget) {
		this.budget = budget;
	}

	public ProjectStatus getStatus() {
		return status == null ? ProjectStatus.DRAFT : status;
	}

	public void setStatus(ProjectStatus status) {
		this.status = status;
	}
}
