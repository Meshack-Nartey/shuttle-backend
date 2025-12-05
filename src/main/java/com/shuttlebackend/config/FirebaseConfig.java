package com.shuttlebackend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@Component
public class FirebaseConfig {

    @Autowired(required = false)
    private Dotenv dotenv;

    @PostConstruct
    public void init() {
        try {
            // prefer OS env var, fall back to .env via Dotenv if available
            String path = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON");
            if ((path == null || path.isBlank()) && dotenv != null) {
                try {
                    path = dotenv.get("FIREBASE_SERVICE_ACCOUNT_JSON");
                } catch (Exception ignored) {
                }
            }

            if (path == null || path.isBlank()) {
                System.out.println("Firebase service account not configured (FIREBASE_SERVICE_ACCOUNT_JSON missing). Skipping Firebase init.");
                return; // not configured
            }

            InputStream serviceAccount = null;

            // support classpath: prefix or direct resource names
            if (path.startsWith("classpath:")) {
                String res = path.substring("classpath:".length());
                serviceAccount = FirebaseConfig.class.getClassLoader().getResourceAsStream(res);
                if (serviceAccount == null) {
                    System.err.println("Firebase service account not found on classpath: " + res);
                }
            } else {
                // first try as classpath resource name (e.g. if user used just the filename placed in resources)
                serviceAccount = FirebaseConfig.class.getClassLoader().getResourceAsStream(path);
                if (serviceAccount == null) {
                    // then try as a file system path
                    File f = new File(path);
                    if (f.exists() && f.isFile()) {
                        serviceAccount = new FileInputStream(f);
                    } else {
                        System.err.println("Firebase service account file not found at path: " + path);
                    }
                }
            }

            if (serviceAccount == null) {
                System.err.println("Failed to load Firebase service account - skipping initialization.");
                return;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase initialized using service account path: " + path);
            }
        } catch (Exception ex) {
            // log and continue; FCM disabled if not configured
            System.err.println("Failed to initialize Firebase: " + ex.getMessage());
        }
    }
}
