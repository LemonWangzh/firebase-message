package com.kars.fire.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
public class FirebaseConfig {


    @Bean(name = "default_firebaseApp")
    public FirebaseApp init() throws IOException {
        ClassPathResource cpr = new ClassPathResource("serviceAccountKey.json");
        if (!cpr.exists())
            throw new IOException("serviceAccountKey not find");
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(cpr.getInputStream()))
                .build();
        return FirebaseApp.initializeApp(options);
    }
}
