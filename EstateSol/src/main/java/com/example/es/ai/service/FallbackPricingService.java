package com.example.es.ai.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.es.ai.dto.PricingResult;
import com.example.es.apartment.entity.Apartment;

@Service
public class FallbackPricingService {

    private static final Map<String, Double> SALE_PRICE = Map.ofEntries(
        Map.entry("Κολωνάκι", 4500.0),
        Map.entry("Παγκράτι", 3200.0),
        Map.entry("Κυψέλη", 2300.0),
        Map.entry("Πετρούπολη", 1900.0),
        Map.entry("Περιστέρι", 2000.0),
        Map.entry("Αιγάλεω", 1850.0),
        Map.entry("Χαλάνδρι", 3300.0),
        Map.entry("Γλυφάδα", 4800.0),
        Map.entry("Πειραιάς", 2400.0)
    );

    private static final Map<String, Double> RENT_PRICE = Map.ofEntries(
        Map.entry("Κολωνάκι", 15.0),
        Map.entry("Παγκράτι", 12.0),
        Map.entry("Κυψέλη", 9.0),
        Map.entry("Πετρούπολη", 7.5),
        Map.entry("Περιστέρι", 8.0),
        Map.entry("Αιγάλεω", 7.0),
        Map.entry("Χαλάνδρι", 12.0),
        Map.entry("Γλυφάδα", 16.0),
        Map.entry("Πειραιάς", 9.0)
    );

    public PricingResult calculate(Apartment apartment) {
        String area = apartment.getArea();
        double sqm = apartment.getSquareMeters();

        double salePerSqm = SALE_PRICE.getOrDefault(area, 1800.0);
        double rentPerSqm = RENT_PRICE.getOrDefault(area, 8.0);

        return new PricingResult(
        	    Double.valueOf(sqm * rentPerSqm),
        	    Double.valueOf(sqm * salePerSqm)
        	);
    }
}