package com.shuttlebackend.services;

import com.shuttlebackend.entities.DeviceToken;
import com.shuttlebackend.entities.Student;
import com.shuttlebackend.entities.TripActivity;
import com.shuttlebackend.repositories.TripActivityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TripReminderSchedulerUnitTest {

    @Mock
    TripActivityRepository tripRepo;

    @Mock
    SimpMessagingTemplate messagingTemplate;

    @Mock
    FcmService fcmService;

    TripReminderScheduler scheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        scheduler = new TripReminderScheduler(tripRepo, messagingTemplate, fcmService);
    }

    @Test
    void sendsRemindersAndMarksNotified() {
        TripActivity t = new TripActivity();
        t.setId(1L);
        Student s = new Student(); s.setId(42);
        t.setStudent(s);
        t.setEstimatedTime(Instant.now().plusSeconds(300));
        t.setReminderScheduledAt(Instant.now().minusSeconds(10));
        t.setNotificationSent(false);

        when(tripRepo.findByNotificationSentFalseAndReminderScheduledAtLessThanEqual(any())).thenReturn(List.of(t));
        when(tripRepo.findByReminderScheduledAtLessThanEqualAndStatusNot(any(), any())).thenReturn(List.of());

        scheduler.runReminders();

        // verify websocket sent
        verify(messagingTemplate, atLeastOnce()).convertAndSend(eq("/topic/student/42/reminder"), any(Map.class), any(Map.class));
        // verify FCM attempted
        verify(fcmService, atLeastOnce()).sendReminderToStudent(eq(42), any(), any(), any());

        // verify tripRepo.save called to persist notificationSent/status change
        ArgumentCaptor<TripActivity> cap = ArgumentCaptor.forClass(TripActivity.class);
        verify(tripRepo, atLeastOnce()).save(cap.capture());
        TripActivity saved = cap.getValue();
        assertThat(saved.getNotificationSent()).isTrue();
        assertThat(saved.getStatus()).isEqualTo("NOTIFIED");
    }
}
