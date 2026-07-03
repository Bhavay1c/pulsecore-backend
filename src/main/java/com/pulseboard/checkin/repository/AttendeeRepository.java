package com.pulseboard.checkin.repository;

import com.pulseboard.checkin.model.Attendee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttendeeRepository extends JpaRepository<Attendee, Long> {

    Optional<Attendee> findByEmailIgnoreCase(String email);

    List<Attendee> findByVipTrue();

    List<Attendee> findByCheckedInTrue();

    long countByCheckedInTrue();

    long countByVipTrue();

    long countByVipTrueAndCheckedInTrue();

    // Backs both the per-session capacity check at check-in time and the
    // session capacity bars on the dashboard.
    long countBySessionIdAndCheckedInTrue(Long sessionId);

    // Most recent check-ins first, for a live activity feed on the dashboard.
    List<Attendee> findTop10ByCheckedInTrueOrderByCheckInTimeDesc();
}
