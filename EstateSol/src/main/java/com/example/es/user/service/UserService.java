package com.example.es.user.service;

import com.example.es.user.entity.UserRole;
import com.example.es.user.entity.User;
import com.example.es.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public User registerUser(User user) {
		if (userRepository.existsByUsername(user.getUsername())) {
			throw new RuntimeException("User already exists!");
		}

		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setRole(UserRole.AMATEUR_USER);

		return userRepository.save(user);
	}

	public User login(String username, String rawPassword) {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new RuntimeException("User not found"));

		if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
			throw new RuntimeException("Invalid Credentials");
		}
		return user;
	}
}
