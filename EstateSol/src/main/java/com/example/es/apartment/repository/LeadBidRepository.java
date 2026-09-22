package com.example.es.apartment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.es.apartment.entity.LeadBid;
import com.example.es.apartment.entity.Apartment;
import com.example.es.user.entity.User;
import java.util.List;

public interface LeadBidRepository extends JpaRepository<LeadBid, Long> {
    List<LeadBid> findByProject(Apartment project);
    List<LeadBid> findByBusinessUser(User businessUser);
    List<LeadBid> findByProjectOwnerUsername(String username);
}
