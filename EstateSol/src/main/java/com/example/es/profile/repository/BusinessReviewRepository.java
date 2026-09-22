package com.example.es.profile.repository;

import com.example.es.profile.entity.BusinessReview;
import com.example.es.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BusinessReviewRepository extends JpaRepository<BusinessReview, Long> {
    List<BusinessReview> findByBusinessUserOrderByCreatedAtDesc(User businessUser);
}
