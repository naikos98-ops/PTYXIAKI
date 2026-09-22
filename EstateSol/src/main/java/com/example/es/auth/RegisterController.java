package com.example.es.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.es.user.dto.RegisterRequest;
import com.example.es.user.entity.UserRole;
import com.example.es.user.entity.User;
import com.example.es.user.repository.UserRepository;

@RestController
public class RegisterController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()
                || request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Username and password are required");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("USERNAME_EXISTS");
        }

        UserRole selectedRole = request.getRole() != null ? request.getRole() : UserRole.AMATEUR_USER;
        if (selectedRole == UserRole.ADMIN) {
            return ResponseEntity.badRequest().body("Cannot register as administrator");
        }
        if (selectedRole == UserRole.CONSTRUCTION_COMPANY || selectedRole == UserRole.REAL_ESTATE_AGENCY) {
            if (request.getAfm() == null || request.getAfm().trim().isEmpty()
                    || request.getGemiNumber() == null || request.getGemiNumber().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("AFM and GEMI number are required for business accounts");
            }
            String afm = request.getAfm().trim();
            if (afm.length() != 9 || !afm.matches("\\d+")) {
                return ResponseEntity.badRequest().body("Greek AFM must be exactly 9 numeric digits");
            }
        }

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(selectedRole);

        if (selectedRole == UserRole.CONSTRUCTION_COMPANY || selectedRole == UserRole.REAL_ESTATE_AGENCY) {
            user.setAfm(request.getAfm().trim());
            user.setGemiNumber(request.getGemiNumber().trim());
            user.setRegion(request.getRegion() != null ? request.getRegion().trim() : null);
            user.setVerified(false);
        } else {
            user.setVerified(false);
        }

        userRepository.save(user);

        return ResponseEntity.ok().build();
    }
}
