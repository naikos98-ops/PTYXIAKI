package com.example.es.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.es.user.entity.UserRole;
import com.example.es.user.entity.User;

public interface UserRepository extends JpaRepository<User,Long> {
	Optional<User> findByUsername(String username);
	
	List<User> findByRole(UserRole role);
	
	boolean existsByUsername(String username);

}
