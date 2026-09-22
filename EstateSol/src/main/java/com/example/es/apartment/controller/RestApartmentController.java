package com.example.es.apartment.controller;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.example.es.apartment.dto.ApartmentCreateDto;
import com.example.es.apartment.dto.ApartmentDto;
import com.example.es.apartment.dto.ApartmentUpdateDto;

import com.example.es.apartment.entity.Apartment;
import com.example.es.apartment.mapper.ApartmentMapper;
import com.example.es.apartment.service.ApartmentService;
import com.example.es.user.entity.User;
import com.example.es.user.repository.UserRepository;
import com.example.es.ai.service.AIProcessingService;

@RestController
@RequestMapping("/api/apartments")
public class RestApartmentController {

	private final ApartmentService apartmentService;
	private final ApartmentMapper apartmentMapper;
	private final UserRepository userRepository;
	private final AIProcessingService aiProcessingService;

	public RestApartmentController(ApartmentService apartmentService,
			ApartmentMapper apartmentMapper, UserRepository userRepository,
			AIProcessingService aiProcessingService) {
		this.apartmentService = apartmentService;
		this.apartmentMapper = apartmentMapper;
		this.userRepository = userRepository;
		this.aiProcessingService = aiProcessingService;

	}

	@GetMapping
	public List<ApartmentDto> getApartments(Authentication auth) {

		return apartmentService.getAllApartments(auth);
	}

	@GetMapping("/{id}")
	public ApartmentDto getById(@PathVariable Long id) {

		Apartment apartment = apartmentService.findById(id);

		return apartmentMapper.toDto(apartment);
	}

	@PreAuthorize("hasAnyRole('AMATEUR_USER','ADMIN')")
	@PostMapping("createApartment")
	public Map<String, Long> createApartment(
			@ModelAttribute ApartmentCreateDto dto,
			@RequestParam("image") MultipartFile image,
			Authentication authentication) {

		String username = authentication.getName();
		User currentUser = userRepository.findByUsername(username)
				.orElseThrow(() -> new RuntimeException("User not found"));

		Long id = apartmentService.createApartment(dto, image, currentUser);
		aiProcessingService.processApartment(id);

		return Map.of("id", id);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("updateasadmin/{id}")
	@ResponseBody
	public void adminUpdateApartment(
			@PathVariable Long id,
			@RequestBody ApartmentUpdateDto dto,
			Authentication authentication) throws AccessDeniedException {

		String username = authentication.getName();
		User currentUser = userRepository.findByUsername(username)
				.orElseThrow();

		apartmentService.adminUpdate(id, dto);
	}

	@PreAuthorize("hasAnyRole('AMATEUR_USER','ADMIN')")
	@PostMapping("updateasuser/{id}")
	@ResponseBody
	public void userUpdateApartment(
			@PathVariable Long id,
			@RequestBody ApartmentUpdateDto dto,
			Authentication authentication) throws AccessDeniedException {

		String username = authentication.getName();
		User currentUser = userRepository.findByUsername(username)
				.orElseThrow();

		apartmentService.userUpdate(id, dto);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("delete/{id}")
	@ResponseBody
	public void adminDeleteApartment(@PathVariable Long id) {
		apartmentService.adminDelete(id);
	}

	@PreAuthorize("hasAnyRole('AMATEUR_USER')")
	@PostMapping("deleteasuser/{id}")
	@ResponseBody
	public void userDeleteApartment(@PathVariable Long id, Authentication authentication) throws AccessDeniedException {
		String username = authentication.getName();
		Apartment apartment = apartmentService.getApartmentById(id);
		if (!apartment.getOwner().getUsername().equals(username)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to delete this apartment");
		}
		apartmentService.adminDelete(id);
	}

	@PostMapping("/{id}/complete-renovation")
	@ResponseBody
	public void completeRenovation(@PathVariable Long id, Authentication authentication) {
		String username = authentication.getName();
		User currentUser = userRepository.findByUsername(username).orElseThrow();
		apartmentService.completeRenovation(id, currentUser);
	}

	@PostMapping("/{id}/reset-renovation")
	@ResponseBody
	public void resetRenovation(@PathVariable Long id, Authentication authentication) {
		String username = authentication.getName();
		User currentUser = userRepository.findByUsername(username).orElseThrow();
		apartmentService.resetRenovation(id, currentUser);
	}

	@PreAuthorize("hasAnyRole('AMATEUR_USER','ADMIN')")
	@PostMapping("/{id}/retry-ai")
	@ResponseBody
	public void retryAiProcessing(@PathVariable Long id, Authentication authentication) {
		User currentUser = userRepository.findByUsername(authentication.getName()).orElseThrow();
		Apartment apartment = apartmentService.getApartmentById(id);
		boolean isOwner = apartment.getOwner().getId().equals(currentUser.getId());
		boolean isAdmin = currentUser.getRole() == com.example.es.user.entity.UserRole.ADMIN;
		if (!isOwner && !isAdmin) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to retry AI processing");
		}
		apartment.setAiProcessed(false);
		apartment.setAiProcessingError(null);
		apartmentService.save(apartment);
		aiProcessingService.processApartment(id);
	}

}
