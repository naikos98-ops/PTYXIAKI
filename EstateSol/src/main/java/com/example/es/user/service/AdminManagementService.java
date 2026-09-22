package com.example.es.user.service;

import com.example.es.apartment.entity.Apartment;
import com.example.es.apartment.entity.ProjectStatus;
import com.example.es.apartment.repository.ApartmentRepository;
import com.example.es.user.entity.User;
import com.example.es.user.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@PreAuthorize("hasRole('ADMIN')")
public class AdminManagementService {

    private final UserRepository userRepository;
    private final ApartmentRepository apartmentRepository;

    public AdminManagementService(UserRepository userRepository, ApartmentRepository apartmentRepository) {
        this.userRepository = userRepository;
        this.apartmentRepository = apartmentRepository;
    }
    public void verifyBusinessIdentity(Long userId, boolean isVerified) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.setVerified(isVerified);
        userRepository.save(user);
    }
    public void overrideUserDocuments(Long userId, String afm, String gemiNumber, String region) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.setAfm(afm);
        user.setGemiNumber(gemiNumber);
        user.setRegion(region);
        userRepository.save(user);
    }
    public void updateProjectGlobalState(Long projectId, ProjectStatus status) {
        Apartment project = apartmentRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project/Apartment not found: " + projectId));
        project.setStatus(status);
        apartmentRepository.save(project);
    }
}
