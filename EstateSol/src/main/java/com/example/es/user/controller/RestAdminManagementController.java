package com.example.es.user.controller;

import com.example.es.apartment.entity.ProjectStatus;
import com.example.es.user.service.AdminManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/management")
@PreAuthorize("hasRole('ADMIN')")
public class RestAdminManagementController {

    private final AdminManagementService adminManagementService;

    public RestAdminManagementController(AdminManagementService adminManagementService) {
        this.adminManagementService = adminManagementService;
    }
    @PostMapping("/verify/{userId}")
    public ResponseEntity<Void> verifyBusiness(@PathVariable Long userId, @RequestParam boolean verified) {
        adminManagementService.verifyBusinessIdentity(userId, verified);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/override/{userId}")
    public ResponseEntity<Void> overrideDocuments(@PathVariable Long userId,
                                                   @RequestParam String afm,
                                                   @RequestParam String gemiNumber,
                                                   @RequestParam(required = false) String region) {
        adminManagementService.overrideUserDocuments(userId, afm, gemiNumber, region);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/project/{projectId}/status")
    public ResponseEntity<Void> updateProjectStatus(@PathVariable Long projectId, @RequestParam ProjectStatus status) {
        adminManagementService.updateProjectGlobalState(projectId, status);
        return ResponseEntity.ok().build();
    }
}
