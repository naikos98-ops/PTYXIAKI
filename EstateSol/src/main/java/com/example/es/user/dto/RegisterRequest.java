package com.example.es.user.dto;

import com.example.es.user.entity.UserRole;

public class RegisterRequest {

    private String username;
    private String password;
    private UserRole role;
    private String afm;
    private String gemiNumber;
    private String region;

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

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
