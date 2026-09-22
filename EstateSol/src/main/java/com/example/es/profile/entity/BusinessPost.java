package com.example.es.profile.entity;

import com.example.es.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "business_post")
public class BusinessPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "business_user_id")
    private User businessUser;

    @Column(length = 2000)
    private String content;

    private String imagePath;

    private LocalDateTime createdAt;

    public BusinessPost() {}

    public BusinessPost(User businessUser, String content, String imagePath) {
        this.businessUser = businessUser;
        this.content = content;
        this.imagePath = imagePath;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getBusinessUser() { return businessUser; }
    public void setBusinessUser(User businessUser) { this.businessUser = businessUser; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
