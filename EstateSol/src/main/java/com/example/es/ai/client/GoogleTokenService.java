package com.example.es.ai.client;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.AccessToken;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;

@Service
public class GoogleTokenService {

    public String getAccessToken() {
        try {
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
                    .createScoped(Collections.singleton("https://www.googleapis.com/auth/cloud-platform"));
            credentials.refreshIfExpired();
            AccessToken token = credentials.getAccessToken();
            return token.getTokenValue();
        } catch (IOException e) {
            throw new RuntimeException("Failed to obtain Google OAuth2 token", e);
        }
    }
}
