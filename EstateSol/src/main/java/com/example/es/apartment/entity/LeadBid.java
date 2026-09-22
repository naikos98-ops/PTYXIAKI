package com.example.es.apartment.entity;

import com.example.es.user.entity.User;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "lead_bids")
public class LeadBid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "apartment_id", nullable = false)
    private Apartment project;

    @ManyToOne
    @JoinColumn(name = "business_user_id", nullable = false)
    private User businessUser;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal estimatedCost;

    @Column(nullable = false, length = 1000)
    private String proposalDetails;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BidStatus status = BidStatus.PENDING;
    public LeadBid() {}

    public LeadBid(Apartment project, User businessUser, BigDecimal estimatedCost, String proposalDetails) {
        this.project = project;
        this.businessUser = businessUser;
        this.estimatedCost = estimatedCost;
        this.proposalDetails = proposalDetails;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Apartment getProject() {
        return project;
    }

    public void setProject(Apartment project) {
        this.project = project;
    }

    public User getBusinessUser() {
        return businessUser;
    }

    public void setBusinessUser(User businessUser) {
        this.businessUser = businessUser;
    }

    public BigDecimal getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(BigDecimal estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public String getProposalDetails() {
        return proposalDetails;
    }

    public void setProposalDetails(String proposalDetails) {
        this.proposalDetails = proposalDetails;
    }

    public BidStatus getStatus() {
        return status;
    }

    public void setStatus(BidStatus status) {
        this.status = status;
    }
}
