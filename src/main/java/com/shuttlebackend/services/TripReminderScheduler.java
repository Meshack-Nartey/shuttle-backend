package com.shuttlebackend.services;

import com.shuttlebackend.entities.TripActivity;
import com.shuttlebackend.repositories.TripActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripReminderScheduler {

    private final TripActivityRepository tripRepo;
    private final SimpMessagingTemplate messagingTemplate;

    // run every 30 seconds - check for reminders to send
    @Scheduled(fixedDelay = 30000, initialDelay = 10000)
    @Transactional
    public void runReminders() {
        Instant now = Instant.now();

        // 1) send pending reminders
        List<TripActivity> due = tripRepo.findByNotificationSentFalseAndReminderScheduledAtLessThanEqual(now);
        for (TripActivity t : due) {
            try {
                Integer studentId = t.getStudent() != null ? t.getStudent().getId() : null;
                long minutesLeft = 0L;
                if (t.getEstimatedTime() != null) {
                    minutesLeft = Math.max(0L, Duration.between(now, t.getEstimatedTime()).toMinutes());
                }
                String message = "Your shuttle will arrive in " + minutesLeft + " minutes";

                // publish to student topic
                if (studentId != null) {
                    String topic = "/topic/student/" + studentId + "/reminder";
                    messagingTemplate.convertAndSend(topic, (Object) java.util.Map.of(
                            "tripId", t.getId(),
                            "message", message,
                            "minutesLeft", minutesLeft,
                            "reminderScheduledAt", t.getReminderScheduledAt()
                    ));
                }

                // update trip activity: mark notification_sent and optionally set status to NOTIFIED
                t.setNotificationSent(true);
                t.setStatus("NOTIFIED");
                tripRepo.save(t);

            } catch (Exception ex) {
                // swallow per-job errors to avoid stopping scheduler
            }
        }

        // 2) mark trips as PAST if 30 minutes have passed since reminderScheduledAt
        Instant cutoff = now.minus(Duration.ofMinutes(30));
        List<TripActivity> old = tripRepo.findByReminderScheduledAtLessThanEqualAndStatusNot(cutoff, "PAST");
        for (TripActivity t : old) {
            try {
                t.setStatus("PAST");
                tripRepo.save(t);

                // Optionally publish to student topic that trip moved to past
                Integer studentId = t.getStudent() != null ? t.getStudent().getId() : null;
                if (studentId != null) {
                    String topic = "/topic/student/" + studentId + "/trip-status";
                    messagingTemplate.convertAndSend(topic, (Object) java.util.Map.of(
                            "tripId", t.getId(),
                            "status", "PAST"
                    ));
                }
            } catch (Exception ex) {
                // ignore
            }
        }
    }
}
