package com.example.es.user.dto;

import com.example.es.user.entity.UserRole;

public class UserDto {
	
	    private Long id;
	    private String username;
	    private UserRole role;
	    private String afm;
	    private String gemiNumber;
	    private boolean isVerified;
	    private String region;

	    public UserDto(Long id, String username, UserRole role, String afm, String gemiNumber, boolean isVerified, String region) {
	        this.id = id;
	        this.username = username;
	        this.role = role;
	        this.afm = afm;
	        this.gemiNumber = gemiNumber;
	        this.isVerified = isVerified;
	        this.region = region;
	    }

	    public Long getId() { return id; }
	    public String getUsername() { return username; }
	    public UserRole getRole() { return role; }
	    public String getAfm() { return afm; }
	    public String getGemiNumber() { return gemiNumber; }
	    public boolean isVerified() { return isVerified; }
	    public String getRegion() { return region; }

}
