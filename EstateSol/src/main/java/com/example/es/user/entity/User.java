package com.example.es.user.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.example.es.apartment.entity.Apartment;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;


@Entity
@Table(name = "users")
public class User {

	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @Column(unique = true, nullable = false)
	    private String username;

	    @Column(nullable = false)
	    private String password;

	    @Enumerated(EnumType.STRING)
	    @Column(nullable = false)
	    private UserRole role;

	    @Column(nullable = false)
	    private boolean enabled = true;

	    @Column(nullable = true)
	    private String afm;

	    @Column(nullable = true)
	    private String gemiNumber;

	    @Column(name = "is_verified")
	    private Boolean isVerified = false;

	    @Column(nullable = true)
	    private String region;

	    @JsonIgnore
	    @OneToMany(
	            mappedBy = "owner",
	            cascade = CascadeType.ALL,
	            orphanRemoval = true
	        )
	        private List<Apartment> apartments = new ArrayList<>();

	    public User() {}

	    public User(String username, String password, UserRole role) {
	        this.username = username;
	        this.password = password;
	        this.role = role;
	    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public UserRole getRole() {
		return role;
	}

	public void setRole(UserRole role) {
		this.role = role;
	}

	public String getAfm() {
		return afm;
	}

	public void setAfm(String afm) {
		this.afm = afm;
	}

	public String getGemiNumber() {
		return gemiNumber;
	}

	public void setGemiNumber(String gemiNumber) {
		this.gemiNumber = gemiNumber;
	}

	public boolean isVerified() {
		return isVerified != null && isVerified;
	}

	public void setVerified(Boolean verified) {
		this.isVerified = verified;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
