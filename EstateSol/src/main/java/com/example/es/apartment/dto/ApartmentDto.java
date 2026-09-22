package com.example.es.apartment.dto;

import com.example.es.user.entity.User;
import com.example.es.apartment.entity.ProjectStatus;
import java.math.BigDecimal;

public record ApartmentDto(
                Long id,
                String area,
                Integer squareMeters,
                Integer floor,
                User owner,

                boolean aiProcessed,
                String aiProcessingError,
                Double estimatedRent,
                Double estimatedPrice,
                String estimatedCost,

                String imagePath,
                String renovatedImagePath,

                String googleMapsPin,
                String region,
                BigDecimal budget,
                ProjectStatus status) {
}
