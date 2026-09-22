package com.example.es.user.controller;

import java.util.Map;
import com.example.es.user.entity.User;
import com.example.es.user.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestMeController {

	private final UserRepository userRepository;

	public RestMeController(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@GetMapping("/api/me")
	public Map<String, Object> me(Authentication auth) {
		User user = userRepository.findByUsername(auth.getName()).orElseThrow();
		return Map.of(
				"id", user.getId(),
				"username", auth.getName(),
				"role", auth.getAuthorities().iterator().next().getAuthority());
	}
}
