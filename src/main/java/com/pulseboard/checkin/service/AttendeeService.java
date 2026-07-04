package com.pulseboard.checkin.service;

import com.pulseboard.checkin.dto.*;
import com.pulseboard.checkin.exception.AttendeeNotFoundException;
import com.pulseboard.checkin.exception.CapacityExceededException;
import com.pulseboard.checkin.exception.DuplicateRegistrationException;
import com.pulseboard.checkin.exception.DuplicateSessionException;
import com.pulseboard.checkin.exception.SessionNotFoundException;
import com.pulseboard.checkin.model.Attendee;
import com.pulseboard.checkin.model.Session;
import com.pulseboard.checkin.repository.AttendeeRepository;
import com.pulseboard.checkin.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AttendeeService {

    private final AttendeeRepository attendeeRepository;
    private final SessionRepository sessionRepository;

    // Overall-event fallback capacity, kept for the top-level dashboard stat.
    // Actual admission decisions are enforced per-session (see assertSessionHasRoom).
    @Value("${event.capacity:200}")
    private int capacity;

    public AttendeeService(AttendeeRepository attendeeRepository, SessionRepository sessionRepository) {
        this.attendeeRepository = attendeeRepository;
        this.sessionRepository = sessionRepository;
    }

    @Transactional
    public AttendeeResponse register(RegisterAttendeeRequest request) {
        rejectDuplicateEmail(request.getEmail());
        Session session = findSessionOrThrow(request.getSessionId());

        Attendee attendee = Attendee.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .ticketId(generateTicketId())
                .session(session)
                .vip(request.isVip())
                .checkedIn(false)
                .build();

        return AttendeeResponse.from(attendeeRepository.save(attendee));
    }

    @Transactional
    public AttendeeResponse walkIn(WalkInRequest request) {
        rejectDuplicateEmail(request.getEmail());
        Session session = findSessionOrThrow(request.getSessionId());
        assertSessionHasRoom(session);

        Attendee attendee = Attendee.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .ticketId(generateTicketId())
                .session(session)
                .vip(false)
                .checkedIn(true)
                .checkInTime(LocalDateTime.now())
                .build();

        return AttendeeResponse.from(attendeeRepository.save(attendee));
    }

    @Transactional
    public AttendeeResponse checkIn(Long id) {
        Attendee attendee = findAttendeeOrThrow(id);

        if (attendee.isCheckedIn()) {
            // Idempotent: re-scanning an already-checked-in badge just returns
            // their current state instead of erroring, since that's the most
            // common real-world front-desk mistake.
            return AttendeeResponse.from(attendee);
        }

        assertSessionHasRoom(attendee.getSession());

        attendee.setCheckedIn(true);
        attendee.setCheckInTime(LocalDateTime.now());
        return AttendeeResponse.from(attendeeRepository.save(attendee));
    }

    @Transactional
    public AttendeeResponse undoCheckIn(Long id) {
        Attendee attendee = findAttendeeOrThrow(id);

        if (!attendee.isCheckedIn()) {
            return AttendeeResponse.from(attendee);
        }

        attendee.setCheckedIn(false);
        attendee.setCheckInTime(null);
        return AttendeeResponse.from(attendeeRepository.save(attendee));
    }

    @Transactional(readOnly = true)
    public List<AttendeeResponse> getAllAttendees(Boolean vipOnly) {
        List<Attendee> attendees = (vipOnly != null && vipOnly)
                ? attendeeRepository.findByVipTrue()
                : attendeeRepository.findAll();

        return attendees.stream().map(AttendeeResponse::from).toList();
    }

    public List<SessionResponse> getAllSessions() {
        return sessionRepository.findAll().stream()
                .map(session -> SessionResponse.of(session, attendeeRepository.countBySessionIdAndCheckedInTrue(session.getId())))
                .toList();
    }

    @Transactional
    public SessionResponse createSession(CreateSessionRequest request) {
        sessionRepository.findByNameIgnoreCase(request.getName()).ifPresent(existing -> {
            throw new DuplicateSessionException("A session named " + request.getName() + " already exists");
        });

        Session session = Session.builder()
                .name(request.getName())
                .capacity(request.getCapacity())
                .build();

        return SessionResponse.of(sessionRepository.save(session), 0);
    }

    @Transactional(readOnly = true)
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
                getAllSessions(),
                totalCheckedIn >= capacity
        );
    }

    private void rejectDuplicateEmail(String email) {
        attendeeRepository.findByEmailIgnoreCase(email).ifPresent(existing -> {
            throw new DuplicateRegistrationException("An attendee with email " + email + " is already registered");
        });
    }

    private Session findSessionOrThrow(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException("No session found with id " + sessionId));
    }

    private Attendee findAttendeeOrThrow(Long id) {
        return attendeeRepository.findById(id)
                .orElseThrow(() -> new AttendeeNotFoundException("No attendee found with id " + id));
    }

    private void assertSessionHasRoom(Session session) {
        long checkedIntoSession = attendeeRepository.countBySessionIdAndCheckedInTrue(session.getId());
        if (checkedIntoSession >= session.getCapacity()) {
            throw new CapacityExceededException(
                    session.getName() + " is at capacity (" + session.getCapacity() + "). Cannot check in additional attendees.");
        }
    }

    private String generateTicketId() {
        return "TCK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
