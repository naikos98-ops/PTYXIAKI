package com.example.es.profile.controller;

import com.example.es.profile.entity.BusinessPost;
import com.example.es.profile.entity.BusinessReview;
import com.example.es.profile.repository.BusinessPostRepository;
import com.example.es.profile.repository.BusinessReviewRepository;
import com.example.es.user.entity.User;
import com.example.es.user.entity.UserRole;
import com.example.es.user.repository.UserRepository;
import com.example.es.apartment.repository.LeadBidRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/business-profile")
public class RestBusinessProfileController {

    private final UserRepository userRepository;
    private final BusinessPostRepository businessPostRepository;
    private final BusinessReviewRepository businessReviewRepository;
    private final LeadBidRepository leadBidRepository;

    public RestBusinessProfileController(UserRepository userRepository,
                                         BusinessPostRepository businessPostRepository,
                                         BusinessReviewRepository businessReviewRepository,
                                         LeadBidRepository leadBidRepository) {
        this.userRepository = userRepository;
        this.businessPostRepository = businessPostRepository;
        this.businessReviewRepository = businessReviewRepository;
        this.leadBidRepository = leadBidRepository;
    }

    @GetMapping("/list")
    public ResponseEntity<List<Map<String, Object>>> listBusinessProfiles() {
        List<User> businessUsers = userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.CONSTRUCTION_COMPANY || u.getRole() == UserRole.REAL_ESTATE_AGENCY)
                .collect(Collectors.toList());

        List<Map<String, Object>> responseList = businessUsers.stream().map(bu -> {
            List<BusinessReview> reviews = businessReviewRepository.findByBusinessUserOrderByCreatedAtDesc(bu);
            double avgRating = 0;
            if (!reviews.isEmpty()) {
                avgRating = reviews.stream().mapToInt(BusinessReview::getRating).average().orElse(0.0);
            }

            Map<String, Object> map = new HashMap<>();
            map.put("id", bu.getId());
            map.put("username", bu.getUsername());
            map.put("role", bu.getRole());
            map.put("afm", bu.getAfm());
            map.put("gemiNumber", bu.getGemiNumber());
            map.put("region", bu.getRegion());
            map.put("isVerified", bu.isVerified());
            map.put("averageRating", avgRating);
            map.put("reviewCount", reviews.size());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getProfile(@PathVariable Long userId, Authentication authentication) {
        User businessUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Business profile not found"));

        if (businessUser.getRole() != UserRole.CONSTRUCTION_COMPANY && businessUser.getRole() != UserRole.REAL_ESTATE_AGENCY) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not a business entity");
        }

        List<BusinessPost> posts = businessPostRepository.findByBusinessUserOrderByCreatedAtDesc(businessUser);
        List<BusinessReview> reviews = businessReviewRepository.findByBusinessUserOrderByCreatedAtDesc(businessUser);
        double avgRating = 0;
        if (!reviews.isEmpty()) {
            avgRating = reviews.stream().mapToInt(BusinessReview::getRating).average().orElse(0.0);
        }
        boolean hasCooperated = false;
        if (authentication != null) {
            String currentUsername = authentication.getName();
            hasCooperated = leadBidRepository.findByProjectOwnerUsername(currentUsername).stream()
                    .anyMatch(bid -> bid.getBusinessUser().getId().equals(userId) && bid.getStatus() == com.example.es.apartment.entity.BidStatus.ACCEPTED);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", businessUser.getId());
        response.put("username", businessUser.getUsername());
        response.put("role", businessUser.getRole());
        response.put("afm", businessUser.getAfm());
        response.put("gemiNumber", businessUser.getGemiNumber());
        response.put("region", businessUser.getRegion());
        response.put("isVerified", businessUser.isVerified());
        response.put("averageRating", avgRating);
        response.put("hasCooperated", hasCooperated);

        response.put("posts", posts.stream().map(p -> {
            Map<String, Object> pm = new HashMap<>();
            pm.put("id", p.getId());
            pm.put("content", p.getContent());
            pm.put("imagePath", p.getImagePath());
            pm.put("createdAt", p.getCreatedAt());
            return pm;
        }).collect(Collectors.toList()));

        response.put("reviews", reviews.stream().map(r -> {
            Map<String, Object> rm = new HashMap<>();
            rm.put("id", r.getId());
            rm.put("authorName", r.getAuthor().getUsername());
            rm.put("rating", r.getRating());
            rm.put("comment", r.getComment());
            rm.put("createdAt", r.getCreatedAt());
            return rm;
        }).collect(Collectors.toList()));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/posts")
    @PreAuthorize("hasAnyRole('CONSTRUCTION_COMPANY', 'REAL_ESTATE_AGENCY')")
    public ResponseEntity<Void> createPost(@RequestParam("content") String content,
                                           @RequestParam(value = "image", required = false) MultipartFile image,
                                           Authentication authentication) {
        String username = authentication.getName();
        User businessUser = userRepository.findByUsername(username).orElseThrow();

        if (!businessUser.isVerified()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Business profile must be verified to post updates");
        }

        String imagePath = null;
        if (image != null && !image.isEmpty()) {
            String filename = UUID.randomUUID() + "_" + image.getOriginalFilename();
            Path path = Paths.get("uploads/posts/" + filename);
            try {
                Files.createDirectories(path.getParent());
                Files.copy(image.getInputStream(), path);
                imagePath = "/uploads/posts/" + filename;
            } catch (IOException e) {
                throw new RuntimeException("Could not store image", e);
            }
        }

        BusinessPost post = new BusinessPost(businessUser, content, imagePath);
        businessPostRepository.save(post);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/{userId}/reviews")
    @PreAuthorize("hasRole('AMATEUR_USER')")
    public ResponseEntity<Void> submitReview(@PathVariable Long userId,
                                             @RequestParam("rating") int rating,
                                             @RequestParam("comment") String comment,
                                             Authentication authentication) {
        if (rating < 1 || rating > 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 10");
        }

        User businessUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Business profile not found"));

        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username).orElseThrow();
        boolean hasCooperated = leadBidRepository.findByProjectOwnerUsername(username).stream()
                .anyMatch(bid -> bid.getBusinessUser().getId().equals(userId) && bid.getStatus() == com.example.es.apartment.entity.BidStatus.ACCEPTED);

        if (!hasCooperated) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only rate business entities you have cooperated with");
        }

        BusinessReview review = new BusinessReview(businessUser, currentUser, rating, comment);
        businessReviewRepository.save(review);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
