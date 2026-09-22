package com.example.es.ai.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;
import com.example.es.apartment.entity.Apartment;

@Service
public class AiFeasibilityService {

    public enum FeasibilityStatus {
        BASIC_HABITABILITY,
        UNFEASIBLE,
        COSMETIC_ONLY,
        OPTIMAL,
        PREMIUM
    }

    public static class FeasibilityResult {
        private final FeasibilityStatus status;
        private final BigDecimal estimatedCost;
        private final String message;
        private final String aiPromptModifier;

        public FeasibilityResult(FeasibilityStatus status, BigDecimal estimatedCost, String message, String aiPromptModifier) {
            this.status = status;
            this.estimatedCost = estimatedCost;
            this.message = message;
            this.aiPromptModifier = aiPromptModifier;
        }

        public FeasibilityStatus getStatus() { return status; }
        public BigDecimal getEstimatedCost() { return estimatedCost; }
        public String getMessage() { return message; }
        public String getAiPromptModifier() { return aiPromptModifier; }
    }

    public FeasibilityResult evaluate(Apartment apartment) {
        double sqm = apartment.getSquareMeters();
        if (sqm <= 0) {
            sqm = 1.0;
        }

        BigDecimal budget = apartment.getBudget();

        if (budget == null) {
            BigDecimal cost = BigDecimal.valueOf(180).multiply(BigDecimal.valueOf(sqm));
            return new FeasibilityResult(
                FeasibilityStatus.BASIC_HABITABILITY,
                cost,
                "Basic Habitability Mode: baseline 180€/τ.μ. για τις απολύτως απαραίτητες εργασίες (μερεμέτια, basic δάπεδα, βάψιμο).",
                "ultra-minimal aesthetic renovation, basic fresh paint, clean linoleum floors, patch repair walls, simple overhead lighting"
            );
        }

        BigDecimal costPerSqm = budget.divide(BigDecimal.valueOf(sqm), 2, RoundingMode.HALF_UP);
        double costPerSqmDouble = costPerSqm.doubleValue();

        if (costPerSqmDouble < 200.0) {
            return new FeasibilityResult(
                FeasibilityStatus.UNFEASIBLE,
                budget,
                "Μη βιώσιμη ανακαίνιση (< 200€/τ.μ.). Παρακαλούμε αυξήστε το budget σας.",
                ""
            );
        } else if (costPerSqmDouble >= 200.0 && costPerSqmDouble < 450.0) {
            return new FeasibilityResult(
                FeasibilityStatus.COSMETIC_ONLY,
                budget,
                "Cosmetic-only renovation (200€ - 450€/τ.μ.): Επιφανειακές αλλαγές (βάψιμο, απλά δάπεδα, φωτιστικά).",
                "cosmetic renovation, neat modern painting, laminate flooring, basic contemporary lighting fixtures, clean surfaces"
            );
        } else if (costPerSqmDouble >= 450.0 && costPerSqmDouble < 800.0) {
            return new FeasibilityResult(
                FeasibilityStatus.OPTIMAL,
                budget,
                "Optimal renovation (450€ - 800€/τ.μ.): Ριζική ανακαίνιση (υδραυλικά, γκρεμίσματα, κουζίνα/μπάνιο).",
                "complete interior renewal, high-quality replacement of kitchen and bathroom fixtures in the same location, luxurious tile flooring, brand new wall finishes, modern spotlights"
            );
        } else {
            return new FeasibilityResult(
                FeasibilityStatus.PREMIUM,
                budget,
                "Premium renovation (>= 800€/τ.μ.): Πολυτελής ανακαίνιση (premium υλικά, smart home, customized carpentry).",
                "ultra-luxury interior design refresh, high-end marble wall details, custom cabinetry, smart ambient LED lighting fixtures, premium designer furniture in the same layout"
            );
        }
    }
}
