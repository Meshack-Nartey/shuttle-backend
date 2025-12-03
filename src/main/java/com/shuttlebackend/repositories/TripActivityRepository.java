package com.shuttlebackend.repositories;

import com.shuttlebackend.entities.TripActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;

public interface TripActivityRepository extends JpaRepository<TripActivity, Long> {

    List<TripActivity> findByStudent_Id(Integer studentId);


    List<TripActivity> findByShuttle_Id(Integer shuttleId);

    TripActivity findTop1ByStudent_IdOrderByActualTimeDesc(Integer studentId);

    // reminders: find activities whose reminder time has arrived and notification not yet sent
    List<TripActivity> findByNotificationSentFalseAndReminderScheduledAtLessThanEqual(Instant when);

    // find activities that should be marked PAST (reminderScheduledAt older than cutoff and not already PAST)
    List<TripActivity> findByReminderScheduledAtLessThanEqualAndStatusNot(Instant when, String statusNot);
}
