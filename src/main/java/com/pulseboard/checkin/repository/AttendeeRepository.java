package com.pulseboard.checkin.repository;

import com.pulseboard.checkin.model.Attendee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttendeeRepository extends JpaRepository<Attendee, Long> {

    Optional<Attendee> findByEmailIgnoreCase(String email);

    // Used for check-in-by-name: case-insensitive match, since front-desk staff
    // won't type names with perfectly consistent casing.
    Optional<Attendee> findByFullNameIgnoreCase(String fullName);

    List<Attendee> findByVipTrue();

    List<Attendee> findByCheckedInTrue();

    long countByCheckedInTrue();

    long countByVipTrue();

    long countByVipTrueAndCheckedInTrue();

    // Most recent check-ins first, for a live activity feed on the dashboard.
    List<Attendee> findTop10ByCheckedInTrueOrderByCheckInTimeDesc();
}
