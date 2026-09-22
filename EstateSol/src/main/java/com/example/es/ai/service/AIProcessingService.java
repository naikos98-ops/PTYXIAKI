package com.example.es.ai.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.example.es.ai.client.GeminiClient;
import com.example.es.ai.dto.PricingResult;
import com.example.es.apartment.entity.Apartment;
import com.example.es.apartment.entity.ProjectStatus;
import com.example.es.apartment.repository.ApartmentRepository;
import com.example.es.storage.ImageStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AIProcessingService {

    private final GeminiClient geminiClient;
    private final ApartmentRepository apartmentRepository;
    private final ImageStorageService imageStorageService;
    private final FallbackPricingService fallbackPricingService;
    private final AiFeasibilityService aiFeasibilityService;
    private static final Logger log = LoggerFactory.getLogger(AIProcessingService.class);

    public AIProcessingService(GeminiClient geminiClient,
            ApartmentRepository apartmentRepository,
            ImageStorageService imageStorageService,
            FallbackPricingService fallbackPricingService,
            AiFeasibilityService aiFeasibilityService) {

        this.geminiClient = geminiClient;
        this.apartmentRepository = apartmentRepository;
        this.imageStorageService = imageStorageService;
        this.fallbackPricingService = fallbackPricingService;
        this.aiFeasibilityService = aiFeasibilityService;
    }
    @Async
    @Transactional
    public void processApartment(Long apartmentId) {
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new RuntimeException("Apartment not found: " + apartmentId));

        apartment.setAiProcessed(false);
        apartment.setAiProcessingError(null);
        apartmentRepository.save(apartment);

        try {
            processApartmentInternal(apartment);
        } catch (Exception e) {
            log.error("AI processing failed for apartment {}", apartmentId, e);
            apartment.setAiProcessed(false);
            apartment.setAiProcessingError("Η δημιουργία της AI εικόνας απέτυχε. Δοκιμάστε ξανά.");
            apartmentRepository.save(apartment);
        }
    }

    private void processApartmentInternal(Apartment apartment) throws java.io.IOException {

        AiFeasibilityService.FeasibilityResult feasibility = aiFeasibilityService.evaluate(apartment);

        if (feasibility.getStatus() == AiFeasibilityService.FeasibilityStatus.UNFEASIBLE) {
            apartment.setEstimatedCost("UNFEASIBLE: " + feasibility.getMessage());
            apartment.setEstimatedRent(0.0);
            apartment.setEstimatedPrice(0.0);
            apartment.setAiProcessed(true);
            apartment.setAiProcessingError(null);
            apartmentRepository.save(apartment);
            log.info("Apartment renovation feasibility check returned UNFEASIBLE. Blocking AI processing.");
            return;
        }
        if (apartment.getBudget() != null) {
            apartment.setEstimatedCost(apartment.getBudget() + " € (Προϋπολογισμός Ιδιοκτήτη)");
        } else {
            apartment.setEstimatedCost(feasibility.getEstimatedCost() + " € (" + feasibility.getMessage() + ")");
        }
        try {
            PricingResult pricing = geminiClient.calculatePricing(apartment);

            apartment.setEstimatedRent(pricing.getRent());
            apartment.setEstimatedPrice(pricing.getSale());
            if (apartment.getBudget() == null && pricing.getRenovationCost() != null) {
                apartment.setEstimatedCost(pricing.getRenovationCost() + " € (AI Estimate)");
            }
        } catch (WebClientResponseException.TooManyRequests e) {

            log.warn("429 from Gemini. Using fallback pricing.");

            PricingResult fallback = fallbackPricingService.calculate(apartment);

            apartment.setEstimatedRent(fallback.getRent());
            apartment.setEstimatedPrice(fallback.getSale());

        } catch (Exception e) {

            log.error("AI pricing failed completely. Reason: " + e.getMessage(), e);

            PricingResult fallback = fallbackPricingService.calculate(apartment);

            apartment.setEstimatedRent(fallback.getRent());
            apartment.setEstimatedPrice(fallback.getSale());
        }
        generateRenovatedImage(apartment);

        apartment.setAiProcessed(true);
        apartment.setAiProcessingError(null);
        if (apartment.getStatus() == ProjectStatus.DRAFT) {
            apartment.setStatus(ProjectStatus.VISUALIZED);
        }
        apartmentRepository.save(apartment);
    }
    private void generateRenovatedImage(Apartment apartment) throws java.io.IOException {
            log.info("Generating renovated image via Gemini 2.5 Flash Image...");

            AiFeasibilityService.FeasibilityResult feasibility = aiFeasibilityService.evaluate(apartment);
            String prompt = "modern apartment renovation, " +
                    feasibility.getAiPromptModifier() + ", " +
                    apartment.getSquareMeters() + " sqm";
            String imagePath = apartment.getImagePath();
            if (imagePath == null || imagePath.isEmpty()) {
                log.warn("Skipping renovation: No image found for apartment {}", apartment.getId());
                return;
            }

            Path path = Paths.get(imagePath);
            if (!Files.exists(path)) {
                path = Paths.get("uploads").resolve(imagePath);
                if (!Files.exists(path)) {
                    log.warn("Skipping renovation: Image file not found at {}", imagePath);
                    return;
                }
            }

            byte[] imageBytes = Files.readAllBytes(path);
            String mimeType = Files.probeContentType(path);
            if (mimeType == null) {
                mimeType = "image/jpeg";
            }

            String base64Image = geminiClient.renovateImage(imageBytes, mimeType, prompt);
            String filename = "renovated_" + apartment.getId() + ".png";
            String savedPath = imageStorageService.saveBase64Image(base64Image, filename);

            apartment.setRenovatedImagePath(savedPath);
            log.info("Saved Gemini image to: {}", savedPath);
    }
}
