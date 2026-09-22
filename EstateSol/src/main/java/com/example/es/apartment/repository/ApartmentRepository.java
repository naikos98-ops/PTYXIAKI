package com.example.es.apartment.repository;

import com.example.es.apartment.entity.Apartment;
import com.example.es.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApartmentRepository extends JpaRepository<Apartment,Long>{
	
	List<Apartment> findByOwner(User owner);
	
	List<Apartment> findByOwnerUsername(String username);


}
