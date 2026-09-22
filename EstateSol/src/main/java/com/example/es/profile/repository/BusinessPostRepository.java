package com.example.es.profile.repository;

import com.example.es.profile.entity.BusinessPost;
import com.example.es.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BusinessPostRepository extends JpaRepository<BusinessPost, Long> {
    List<BusinessPost> findByBusinessUserOrderByCreatedAtDesc(User businessUser);
}
