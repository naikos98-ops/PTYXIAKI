package com.example.es.apartment.mapper;

import org.springframework.stereotype.Component;

import com.example.es.apartment.dto.ApartmentDto;
import com.example.es.apartment.dto.ApartmentUpdateDto;
import com.example.es.apartment.entity.Apartment;

@Component
public class ApartmentMapper {

    public static void updateEntity(Apartment a, ApartmentUpdateDto dto) {
        if (dto.getArea() != null)
            a.setArea(dto.getArea());
        if (dto.getSquareMeters() != null)
            a.setSquareMeters(dto.getSquareMeters());
        if (dto.getFloor() != null)
            a.setFloor(dto.getFloor());
        if (dto.getEstimatedPrice() != null)
            a.setEstimatedPrice(dto.getEstimatedPrice());
        if (dto.getEstimatedRent() != null)
            a.setEstimatedRent(dto.getEstimatedRent());
        if (dto.getEstimatedCost() != null)
            a.setEstimatedCost(dto.getEstimatedCost());
        if (dto.getGoogleMapsPin() != null)
            a.setGoogleMapsPin(dto.getGoogleMapsPin());
        if (dto.getRegion() != null)
            a.setRegion(dto.getRegion());
        if (dto.getBudget() != null)
            a.setBudget(dto.getBudget());
        if (dto.getStatus() != null)
            a.setStatus(dto.getStatus());
    }

    public ApartmentDto toDto(Apartment a) {
        String aiProcessingError = a.getAiProcessingError();
        boolean missingExpectedImage = a.isAiProcessed()
                && (a.getRenovatedImagePath() == null || a.getRenovatedImagePath().isBlank())
                && (a.getEstimatedCost() == null || !a.getEstimatedCost().startsWith("UNFEASIBLE:"));
        if (aiProcessingError == null && missingExpectedImage) {
            aiProcessingError = "Δεν δημιουργήθηκε AI εικόνα ανακαίνισης. Δοκιμάστε ξανά.";
        }

        return new ApartmentDto(
                a.getId(),
                a.getArea(),
                a.getSquareMeters(),
                a.getFloor(),
                a.getOwner(),

                a.isAiProcessed(),
                aiProcessingError,
                a.getEstimatedRent(),
                a.getEstimatedPrice(),
                a.getEstimatedCost(),

                a.getImagePath(),
                a.getRenovatedImagePath(),

                a.getGoogleMapsPin(),
                a.getRegion(),
                a.getBudget(),
                a.getStatus());
    }
}
