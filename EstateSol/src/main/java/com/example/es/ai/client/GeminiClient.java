package com.example.es.ai.client;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.multipart.MultipartFile;

import com.example.es.ai.dto.PricingResult;
import com.example.es.apartment.entity.Apartment;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class GeminiClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final GoogleTokenService tokenService;
    private final String projectId;

    public GeminiClient(
            @Value("${google.cloud.project-id}") String projectId,
            @Value("${google.cloud.region}") String region,
            GoogleTokenService tokenService,
            ObjectMapper objectMapper) {

        this.projectId = projectId;
        this.tokenService = tokenService;
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(32 * 1024 * 1024))
                .build();
    }

    public PricingResult calculatePricing(Apartment apartment) {
        String pricingRegion = "us-central1";

        String vertexUrl = String.format(
                "https://%s-aiplatform.googleapis.com/v1/projects/%s/locations/%s/publishers/google/models/gemini-2.5-flash:generateContent",
                pricingRegion, projectId, pricingRegion);
        String prompt = String.format("""
                Act as a real estate expert in Greece.
                Apartment: %d sqm, Floor: %d, Area: %s.
                Return ONLY a JSON object (no markdown) with these keys:
                { "rent": number, "sale": number, "renovationCost": number }
                """,
                apartment.getSquareMeters(), apartment.getFloor(),
                apartment.getArea());
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of(
                        "temperature", 0.2,
                        "maxOutputTokens", 2048));

        System.out.println(">>> Sending Pricing Request to: " + vertexUrl);

        try {
            String response = webClient.post()
                    .uri(vertexUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenService.getAccessToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parsePricing(response);

        } catch (WebClientResponseException e) {
            System.err.println("Vertex Pricing Error (" + e.getStatusCode() + "): " + e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Pricing failed: " + e.getMessage(), e);
        }
    }

    public String renovateImage(MultipartFile imageFile, String userInstructions) throws IOException {
        return renovateImage(imageFile.getBytes(), imageFile.getContentType(), userInstructions);
    }

    public String renovateImage(byte[] imageBytes, String mimeType, String userInstructions) throws IOException {
        String vertexUrl = String.format(
                "https://aiplatform.googleapis.com/v1/projects/%s/locations/global/publishers/google/models/gemini-2.5-flash-image:generateContent",
                projectId);

        String base64InputImage = Base64.getEncoder().encodeToString(imageBytes);
        String editPrompt = "Edit the supplied apartment photo into a realistic renovated version. " +
                "Preserve the exact camera angle, perspective, room geometry, walls, windows, doors and columns. " +
                "Change only finishes, paint, flooring, lighting and furniture. Do not alter the architecture. " +
                "Return an edited image. Renovation brief: " + userInstructions;

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(
                                Map.of("inlineData", Map.of(
                                        "mimeType", mimeType,
                                        "data", base64InputImage)),
                                Map.of("text", editPrompt)))),
                "generationConfig", Map.of(
                        "responseModalities", List.of("TEXT", "IMAGE")));

        String response;
        try {
            response = webClient.post()
                    .uri(vertexUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenService.getAccessToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            System.err.println("Gemini image error: " + e.getResponseBodyAsString());
            throw new RuntimeException("Vertex AI Error: " + e.getStatusCode(), e);
        }

        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode parts = root.path("candidates").path(0).path("content").path("parts");
            for (JsonNode part : parts) {
                JsonNode inlineData = part.path("inlineData");
                if (inlineData.isMissingNode()) {
                    inlineData = part.path("inline_data");
                }
                String imageData = inlineData.path("data").asText();
                if (!imageData.isBlank()) {
                    return imageData;
                }
            }
            throw new RuntimeException("Gemini returned no image data");
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Gemini image response", e);
        }
    }

    private PricingResult parsePricing(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            String jsonText = root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            if (jsonText.startsWith("```")) {
                jsonText = jsonText.replaceAll("^```json", "").replaceAll("^```", "").replaceAll("```$", "").trim();
            }
            return objectMapper.readValue(jsonText, PricingResult.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Gemini pricing response. Response was: " + response, e);
        }
    }
}
