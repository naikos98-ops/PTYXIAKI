package com.example.es.user.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.es.user.dto.UserDto;
import com.example.es.user.entity.UserRole;
import com.example.es.user.entity.User;
import com.example.es.user.repository.UserRepository;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/admin/users")
public class RestAdminUserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RestAdminUserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;

    }
    @GetMapping
    public List<UserDto> loadUsers() {

        return userRepository.findAll()
                .stream()
                .filter(user -> user.getRole() != UserRole.ADMIN)
                .map(user -> new UserDto(
                        user.getId(),
                        user.getUsername(),
                        user.getRole(),
                        user.getAfm(),
                        user.getGemiNumber(),
                        user.isVerified(),
                        user.getRegion()))
                .toList();
    }
    @PostMapping("/{id}/username")
    public ResponseEntity<Void> updateUsername(
            @PathVariable Long id,
            @RequestParam String username) {

        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        user.setUsername(username.trim());
        userRepository.save(user);

        return ResponseEntity.ok().build();
    }
    @PostMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        userRepository.delete(user);
        return ResponseEntity.ok().build();
    }
    @PostMapping
    public ResponseEntity<String> createUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false, defaultValue = "false") boolean admin) {
        if (username == null || username.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (userRepository.existsByUsername(username)) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("USERNAME_EXISTS");
        }
        if (role != null && (role == UserRole.CONSTRUCTION_COMPANY || role == UserRole.REAL_ESTATE_AGENCY)) {
            return ResponseEntity.badRequest().body("Admins cannot manually create business accounts.");
        }
        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(passwordEncoder.encode(password));
        if (role != null) {
            user.setRole(role);
        } else {
            user.setRole(admin ? UserRole.ADMIN : UserRole.AMATEUR_USER);
        }

        userRepository.save(user);

        return ResponseEntity.ok().build();
    }
}
