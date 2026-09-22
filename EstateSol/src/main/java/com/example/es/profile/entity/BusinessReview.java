package com.example.es.profile.entity;

import com.example.es.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "business_review")
public class BusinessReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "business_user_id")
    private User businessUser;

    @ManyToOne(optional = false)
    @JoinColumn(name = "author_id")
    private User author;

    private int rating;

    @Column(length = 1000)
    private String comment;

    private LocalDateTime createdAt;

    public BusinessReview() {}

    public BusinessReview(User businessUser, User author, int rating, String comment) {
        this.businessUser = businessUser;
        this.author = author;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getBusinessUser() { return businessUser; }
    public void setBusinessUser(User businessUser) { this.businessUser = businessUser; }
    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
