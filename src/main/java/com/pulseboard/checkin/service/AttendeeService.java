package com.pulseboard.checkin.service;

import com.pulseboard.checkin.dto.*;
import com.pulseboard.checkin.exception.AttendeeNotFoundException;
import com.pulseboard.checkin.exception.CapacityExceededException;
import com.pulseboard.checkin.exception.DuplicateRegistrationException;
import com.pulseboard.checkin.model.Attendee;
import com.pulseboard.checkin.repository.AttendeeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AttendeeService {

    private final AttendeeRepository attendeeRepository;

    // Configurable per event via application.yml (event.capacity) so this
    // isn't hardcoded to one conference size.
    @Value("${event.capacity:200}")
    private int capacity;

    public AttendeeService(AttendeeRepository attendeeRepository) {
        this.attendeeRepository = attendeeRepository;
    }

    @Transactional
    public AttendeeResponse register(RegisterAttendeeRequest request) {
        attendeeRepository.findByEmailIgnoreCase(request.getEmail())
                .ifPresent(existing -> {
                    throw new DuplicateRegistrationException(
                            "An attendee with email " + request.getEmail() + " is already registered");
                });

        Attendee attendee = Attendee.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .vip(request.isVip())
                .checkedIn(false)
                .build();

        return AttendeeResponse.from(attendeeRepository.save(attendee));
    }

    @Transactional
    public AttendeeResponse checkIn(CheckInRequest request) {
        Attendee attendee = attendeeRepository.findByFullNameIgnoreCase(request.getFullName())
                .orElseThrow(() -> new AttendeeNotFoundException(
                        "No registered attendee found with name \"" + request.getFullName() + "\""));

        if (attendee.isCheckedIn()) {
            // Idempotent: re-scanning an already-checked-in badge just returns
            // their current state instead of erroring, since that's the most
            // common real-world front-desk mistake.
            return AttendeeResponse.from(attendee);
        }

        long currentlyCheckedIn = attendeeRepository.countByCheckedInTrue();
        if (currentlyCheckedIn >= capacity) {
            throw new CapacityExceededException(
                    "Event is at capacity (" + capacity + "). Cannot check in additional attendees.");
        }

        attendee.setCheckedIn(true);
        attendee.setCheckInTime(LocalDateTime.now());
        return AttendeeResponse.from(attendeeRepository.save(attendee));
    }

    public List<AttendeeResponse> getAllAttendees(Boolean vipOnly) {
        List<Attendee> attendees = (vipOnly != null && vipOnly)
                ? attendeeRepository.findByVipTrue()
                : attendeeRepository.findAll();

        return attendees.stream().map(AttendeeResponse::from).toList();
    }

    public DashboardResponse getDashboard() {
        long totalRegistered = attendeeRepository.count();
        long totalCheckedIn = attendeeRepository.countByCheckedInTrue();
        long vipCount = attendeeRepository.countByVipTrue();
        long vipCheckedIn = attendeeRepository.countByVipTrueAndCheckedInTrue();
        long spotsRemaining = Math.max(0, capacity - totalCheckedIn);

        List<AttendeeResponse> recentCheckIns = attendeeRepository
                .findTop10ByCheckedInTrueOrderByCheckInTimeDesc()
                .stream()
                .map(AttendeeResponse::from)
                .toList();

        return new DashboardResponse(
                capacity,
                totalRegistered,
                totalCheckedIn,
                spotsRemaining,
                vipCount,
                vipCheckedIn,
                recentCheckIns,
                totalCheckedIn >= capacity
        );
    }
}
