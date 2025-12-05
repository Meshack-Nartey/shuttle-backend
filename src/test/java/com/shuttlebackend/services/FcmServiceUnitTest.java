package com.shuttlebackend.services;

import com.shuttlebackend.repositories.DeviceTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.when;

class FcmServiceUnitTest {

    @Mock
    DeviceTokenRepository deviceRepo;

    FcmService fcmService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        fcmService = new FcmService(deviceRepo);
    }

    @Test
    void sendReminderToStudent_noTokens_noError() {
        when(deviceRepo.findByStudent_IdAndIsActiveTrue(123)).thenReturn(java.util.List.of());
        fcmService.sendReminderToStudent(123, "Title", "Body", java.util.Map.of("k","v"));
        // no exception means success for this simple test
    }
}

