package com.example.es.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import org.springframework.stereotype.Service;

@Service
public class ImageStorageService {

    private static final String BASE_DIR = "uploads/";

    public String saveBase64Image(String base64, String filename) {
        try {
            byte[] data = Base64.getDecoder().decode(base64);
            Path path = Paths.get(BASE_DIR + filename);

            Files.createDirectories(path.getParent());
            Files.write(path, data);

            return path.toString();

        } catch (Exception e) {
            throw new RuntimeException("Failed to save image", e);
        }
    }

    public String saveBytes(byte[] data, String filename) {
        try {
            Path path = Paths.get("uploads/" + filename);
            Files.createDirectories(path.getParent());
            Files.write(path, data);
            return path.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store image", e);
        }
    }

    public String getImageBase64(String filename) {
        try {
            Path path = Paths.get(filename);
            if (!Files.exists(path)) {
                path = Paths.get(BASE_DIR + filename);
            }

            if (!Files.exists(path)) {
                return null;
            }

            byte[] bytes = Files.readAllBytes(path);
            return Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read image", e);
        }
    }
}