package com.shuttlebackend.services;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.shuttlebackend.entities.DeviceToken;
import com.shuttlebackend.repositories.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FcmService {

    private final DeviceTokenRepository deviceRepo;

    public void sendReminderToToken(DeviceToken dt, String title, String body, Map<String, String> data) {
        if (dt == null || dt.getToken() == null) return;
        Message.Builder mb = Message.builder().setToken(dt.getToken());
        if (title != null || body != null) {
            // build Notification using builder (Notification constructor is not public)
            com.google.firebase.messaging.Notification.Builder nb = com.google.firebase.messaging.Notification.builder();
            if (title != null) nb.setTitle(title);
            if (body != null) nb.setBody(body);
            mb.setNotification(nb.build());
        }
        if (data != null) mb.putAllData(data);

        try {
            String resp = FirebaseMessaging.getInstance().send(mb.build());
            // success
        } catch (FirebaseMessagingException e) {
             // handle invalid token errors - mark token inactive
             // e.getErrorCode() may return an enum (ErrorCode) in newer SDKs. Convert to string safely.
             String code;
             try {
                 Object ec = e.getErrorCode();
                 code = ec != null ? ec.toString() : null;
             } catch (Throwable ignore) {
                 code = null;
             }
             if (code == null) {
                 String msg = e.getMessage();
                 code = msg != null ? msg : null;
             }
             if (code != null) {
                 String lc = code.toLowerCase();
                 // common indicators that the token is invalid/unknown
                 if (lc.contains("registration-token-not-registered") || lc.contains("not-registered") || lc.contains("invalid-argument") || lc.contains("invalid-registration-token") || lc.contains("unregistered")) {
                     // mark inactive
                     dt.setIsActive(false);
                     deviceRepo.save(dt);
                 }
            }
        } catch (Exception ex) {
            // log
         }
    }

    public void sendReminderToStudent(Integer studentId, String title, String body, Map<String, String> data) {
        List<DeviceToken> tokens = deviceRepo.findByStudent_IdAndIsActiveTrue(studentId);
        if (tokens == null || tokens.isEmpty()) return;
        for (DeviceToken dt : tokens) {
            sendReminderToToken(dt, title, body, data);
        }
    }
}
