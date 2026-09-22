package com.example.es.apartment.controller;

import com.example.es.apartment.dto.LeadPoolDto;
import com.example.es.apartment.entity.Apartment;
import com.example.es.apartment.entity.BidStatus;
import com.example.es.apartment.entity.LeadBid;
import com.example.es.apartment.entity.ProjectStatus;
import com.example.es.apartment.repository.ApartmentRepository;
import com.example.es.apartment.repository.LeadBidRepository;
import com.example.es.user.entity.UserRole;
import com.example.es.user.entity.User;
import com.example.es.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pool")
public class RestLeadPoolController {

    private final ApartmentRepository apartmentRepository;
    private final LeadBidRepository leadBidRepository;
    private final UserRepository userRepository;

    public RestLeadPoolController(ApartmentRepository apartmentRepository,
                                  LeadBidRepository leadBidRepository,
                                  UserRepository userRepository) {
        this.apartmentRepository = apartmentRepository;
        this.leadBidRepository = leadBidRepository;
        this.userRepository = userRepository;
    }
    @GetMapping
    public List<LeadPoolDto> getPoolLeads(Authentication authentication) {
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        ProjectStatus requiredStatus;
        if (currentUser.getRole() == UserRole.CONSTRUCTION_COMPANY) {
            requiredStatus = ProjectStatus.POOL_CONSTRUCTION;
        } else if (currentUser.getRole() == UserRole.REAL_ESTATE_AGENCY) {
            requiredStatus = ProjectStatus.POOL_REAL_ESTATE;
        } else if (currentUser.getRole() == UserRole.ADMIN) {
            return apartmentRepository.findAll().stream()
                    .filter(a -> a.getStatus() == ProjectStatus.POOL_CONSTRUCTION || a.getStatus() == ProjectStatus.POOL_REAL_ESTATE)
                    .map(a -> new LeadPoolDto(
                            a.getId(), a.getRegion(), a.getArea(), a.getSquareMeters(), a.getFloor(),
                            a.getEstimatedCost(), a.getBudget(), a.getStatus(), true,
                            a.getGoogleMapsPin(), a.getOwner().getUsername(),
                            a.getImagePath(), a.getRenovatedImagePath()
                    ))
                    .collect(Collectors.toList());
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to view lead pool");
        }
        List<LeadBid> companyBids = leadBidRepository.findByBusinessUser(currentUser);

        return apartmentRepository.findAll().stream()
                .filter(a -> a.getStatus() == requiredStatus)
                .map(a -> {
                    boolean isUnlocked = companyBids.stream()
                            .anyMatch(b -> b.getProject().getId().equals(a.getId()) && b.getStatus() == BidStatus.ACCEPTED);
                    return new LeadPoolDto(
                            a.getId(), a.getRegion(), a.getArea(), a.getSquareMeters(), a.getFloor(),
                            a.getEstimatedCost(), a.getBudget(), a.getStatus(), isUnlocked,
                            a.getGoogleMapsPin(), a.getOwner().getUsername(),
                            a.getImagePath(), a.getRenovatedImagePath()
                    );
                })
                .collect(Collectors.toList());
    }
    @PreAuthorize("hasAnyRole('CONSTRUCTION_COMPANY', 'REAL_ESTATE_AGENCY')")
    @PostMapping("/{projectId}/bid")
    public ResponseEntity<Void> submitBid(@PathVariable Long projectId,
                                          @RequestParam BigDecimal estimatedCost,
                                          @RequestParam String proposalDetails,
                                          Authentication authentication) {
        String username = authentication.getName();
        User businessUser = userRepository.findByUsername(username).orElseThrow();
        if (!businessUser.isVerified()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your business account is pending administrator verification.");
        }

        Apartment project = apartmentRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        if (businessUser.getRole() == UserRole.CONSTRUCTION_COMPANY) {
            if (project.getStatus() != ProjectStatus.POOL_CONSTRUCTION) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project is not open for construction bids");
            }
        } else if (businessUser.getRole() == UserRole.REAL_ESTATE_AGENCY) {
            if (project.getStatus() != ProjectStatus.POOL_REAL_ESTATE) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project is not open for real estate agency listings");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to submit bids");
        }

        LeadBid bid = new LeadBid(project, businessUser, estimatedCost, proposalDetails);
        leadBidRepository.save(bid);

        return ResponseEntity.ok().build();
    }
    @GetMapping("/bids")
    public List<LeadBid> getMyBids(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();

        if (user.getRole() == UserRole.ADMIN) {
            return leadBidRepository.findAll();
        } else if (user.getRole() == UserRole.AMATEUR_USER) {
            return leadBidRepository.findByProjectOwnerUsername(username);
        } else {
            return leadBidRepository.findByBusinessUser(user);
        }
    }
    @PostMapping("/bids/{bidId}/accept")
    public ResponseEntity<Void> acceptBid(@PathVariable Long bidId, Authentication authentication) {
        String username = authentication.getName();
        LeadBid bid = leadBidRepository.findById(bidId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bid not found"));

        if (!bid.getProject().getOwner().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your project");
        }

        bid.setStatus(BidStatus.ACCEPTED);
        leadBidRepository.save(bid);
        Apartment project = bid.getProject();
        project.setStatus(ProjectStatus.CONTRACT_LOCKED);
        apartmentRepository.save(project);
        List<LeadBid> otherBids = leadBidRepository.findByProject(project);
        for (LeadBid other : otherBids) {
            if (!other.getId().equals(bid.getId())) {
                other.setStatus(BidStatus.REJECTED);
                leadBidRepository.save(other);
            }
        }

        return ResponseEntity.ok().build();
    }
    @PostMapping("/bids/{bidId}/reject")
    public ResponseEntity<Void> rejectBid(@PathVariable Long bidId, Authentication authentication) {
        String username = authentication.getName();
        LeadBid bid = leadBidRepository.findById(bidId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bid not found"));

        if (!bid.getProject().getOwner().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your project");
        }

        bid.setStatus(BidStatus.REJECTED);
        leadBidRepository.save(bid);

        return ResponseEntity.ok().build();
    }
    @PostMapping("/publish/{projectId}")
    public ResponseEntity<Void> publishToPool(@PathVariable Long projectId,
                                              @RequestParam(required = false) String target,
                                              Authentication authentication) {
        String username = authentication.getName();
        Apartment project = apartmentRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        if (!project.getOwner().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your project");
        }

        if (project.getStatus() == ProjectStatus.CONTRACT_LOCKED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Το ακίνητο είναι ήδη υπό ενεργό συμβόλαιο / ενοικίαση.");
        }

        if ("REAL_ESTATE".equalsIgnoreCase(target) || project.getStatus() == ProjectStatus.COMPLETED) {
            project.setStatus(ProjectStatus.POOL_REAL_ESTATE);
        } else {
            project.setStatus(ProjectStatus.POOL_CONSTRUCTION);
        }
        apartmentRepository.save(project);

        return ResponseEntity.ok().build();
    }
}
