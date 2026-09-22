package com.example.es.apartment.service;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.es.apartment.dto.ApartmentCreateDto;
import com.example.es.apartment.dto.ApartmentDto;
import com.example.es.apartment.dto.ApartmentUpdateDto;
import com.example.es.apartment.entity.Apartment;
import com.example.es.apartment.entity.ProjectStatus;
import com.example.es.apartment.mapper.ApartmentMapper;
import com.example.es.apartment.repository.ApartmentRepository;
import com.example.es.user.entity.UserRole;
import com.example.es.user.entity.User;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ApartmentService {

	private final ApartmentRepository apartmentRepository;
	private final ApartmentMapper apartmentMapper;
	private final com.example.es.apartment.repository.LeadBidRepository leadBidRepository;

	public ApartmentService(ApartmentRepository apartmentRepository,
			ApartmentMapper apartmentMapper,
			com.example.es.apartment.repository.LeadBidRepository leadBidRepository) {
		this.apartmentRepository = apartmentRepository;
		this.apartmentMapper = apartmentMapper;
		this.leadBidRepository = leadBidRepository;
	}

	public Long createApartment(
			ApartmentCreateDto dto,
			MultipartFile image,
			User currentUser) {

		String imagePath = storeImage(image);

		Apartment apartment = new Apartment();
		apartment.setOwner(currentUser);
		apartment.setArea(dto.getArea());
		apartment.setSquareMeters(dto.getSquareMeters());
		apartment.setFloor(dto.getFloor());
		apartment.setImagePath(imagePath);
		apartment.setGoogleMapsPin(dto.getGoogleMapsPin());
		apartment.setRegion(dto.getRegion());
		apartment.setBudget(dto.getBudget());
		apartment.setStatus(ProjectStatus.DRAFT);
		Apartment savedApartment = apartmentRepository.save(apartment);

		return apartment.getId();
	}

	public Apartment save(Apartment apartment) {
		return apartmentRepository.save(apartment);
	}

	public Apartment getApartmentById(Long id) {
		return apartmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Apartment does not exist!"));
	}
	public List<ApartmentDto> getAllApartments(Authentication auth) {
		boolean isAdmin = auth.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
		if (isAdmin) {
			return apartmentRepository.findAll()
					.stream()
					.map(apartmentMapper::toDto)
					.toList();
		}
		String username = auth.getName();

		return apartmentRepository.findByOwnerUsername(username)
				.stream()
				.map(apartmentMapper::toDto)
				.toList();
	}

	public Apartment findById(Long id) {
		return apartmentRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND,
						"Apartment not found"));
	}
	public Apartment updateApartment(
			Long apartmentId,
			ApartmentCreateDto dto,
			User currentUser) throws AccessDeniedException {

		Apartment apartment = apartmentRepository.findById(apartmentId)
				.orElseThrow(() -> new EntityNotFoundException("Apartment not found"));
		if (!canEdit(apartment, currentUser)) {
			throw new AccessDeniedException("Not allowed to edit apartment");
		}
		apartment.setArea(dto.getArea());
		apartment.setSquareMeters(dto.getSquareMeters());
		apartment.setFloor(dto.getFloor());

		return apartmentRepository.save(apartment);
	}
	public Apartment getApartmentForEdit(Long apartmentId, User currentUser) throws AccessDeniedException {

		Apartment apartment = apartmentRepository.findById(apartmentId)
				.orElseThrow(() -> new EntityNotFoundException("Apartment not found"));

		if (!canEdit(apartment, currentUser)) {
			throw new AccessDeniedException("Not allowed to edit apartment");
		}

		return apartment;
	}
	public void deleteApartment(Long apartmentId, User currentUser) throws AccessDeniedException {

		Apartment apartment = apartmentRepository.findById(apartmentId)
				.orElseThrow(() -> new EntityNotFoundException("Apartment not found"));

		if (!canEdit(apartment, currentUser)) {
			throw new AccessDeniedException("You do not have access to delete this aprtment");
		}

		apartmentRepository.deleteById(apartmentId);
	}

	public Apartment adminUpdate(Long id, ApartmentUpdateDto dto) {

		Apartment existing = getApartmentById(id);
		ApartmentMapper.updateEntity(existing, dto);
		return apartmentRepository.save(existing);
	}

	public Apartment userUpdate(Long id, ApartmentUpdateDto dto) {

		Apartment existing = getApartmentById(id);
		ApartmentMapper.updateEntity(existing, dto);
		List<com.example.es.apartment.entity.LeadBid> bids = leadBidRepository.findByProject(existing);
		if (bids != null && !bids.isEmpty()) {
			leadBidRepository.deleteAll(bids);
		}

		return apartmentRepository.save(existing);
	}

	public void adminDelete(Long id) {

		Apartment existing = getApartmentById(id);
		List<com.example.es.apartment.entity.LeadBid> bids = leadBidRepository.findByProject(existing);
		if (bids != null && !bids.isEmpty()) {
			leadBidRepository.deleteAll(bids);
		}
		apartmentRepository.delete(existing);
	}

	public void completeRenovation(Long id, User currentUser) {
		Apartment apartment = getApartmentById(id);

		boolean isOwner = apartment.getOwner().getId().equals(currentUser.getId());
		boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;
		boolean isConstructor = false;

		if (currentUser.getRole() == UserRole.CONSTRUCTION_COMPANY) {
			isConstructor = leadBidRepository.findByProject(apartment).stream()
					.anyMatch(bid -> bid.getBusinessUser().getId().equals(currentUser.getId()) && bid.getStatus() == com.example.es.apartment.entity.BidStatus.ACCEPTED);
		}

		if (!isOwner && !isAdmin && !isConstructor) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to complete renovation for this project");
		}

		if (apartment.getStatus() != ProjectStatus.CONTRACT_LOCKED) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project is not in CONTRACT_LOCKED status");
		}

		apartment.setStatus(ProjectStatus.COMPLETED);
		apartmentRepository.save(apartment);
	}

	public void resetRenovation(Long id, User currentUser) {
		Apartment apartment = getApartmentById(id);

		boolean isOwner = apartment.getOwner().getId().equals(currentUser.getId());
		boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;

		if (!isOwner && !isAdmin) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to reset renovation for this project");
		}

		apartment.setStatus(ProjectStatus.DRAFT);

		List<com.example.es.apartment.entity.LeadBid> bids = leadBidRepository.findByProject(apartment);
		if (bids != null && !bids.isEmpty()) {
			leadBidRepository.deleteAll(bids);
		}

		apartmentRepository.save(apartment);
	}

	private String storeImage(MultipartFile image) {

		if (image == null || image.isEmpty()) {
			throw new IllegalArgumentException("Image is required");
		}

		String filename = UUID.randomUUID() + "_" + image.getOriginalFilename();
		Path path = Paths.get("uploads/apartments/" + filename);

		try {
			Files.createDirectories(path.getParent());
			Files.write(path, image.getBytes());
		} catch (IOException e) {
			throw new RuntimeException("Failed to store image", e);
		}

		return path.toString();
	}

	private boolean isAdmin(User user) {
		return user.getRole() == UserRole.ADMIN;
	}

	private boolean canEdit(Apartment apartment, User user) {
		return isAdmin(user) || apartment.getOwner().equals(user);
	}

}
