package com.pulseboard.checkin;

import com.pulseboard.checkin.dto.RegisterAttendeeRequest;
import com.pulseboard.checkin.exception.AttendeeNotFoundException;
import com.pulseboard.checkin.exception.CapacityExceededException;
import com.pulseboard.checkin.exception.DuplicateRegistrationException;
import com.pulseboard.checkin.model.Session;
import com.pulseboard.checkin.repository.AttendeeRepository;
import com.pulseboard.checkin.repository.SessionRepository;
import com.pulseboard.checkin.service.AttendeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AttendeeServiceTest {

    @Autowired
    private AttendeeService attendeeService;

    @Autowired
    private AttendeeRepository attendeeRepository;

    @Autowired
    private SessionRepository sessionRepository;

    private Long roomyId;
    private Long tinySessionId;

    @BeforeEach
    void cleanSlate() {
        attendeeRepository.deleteAll();
        sessionRepository.deleteAll();
        // Keep capacity small and predictable for the capacity test below.
        ReflectionTestUtils.setField(attendeeService, "capacity", 2);

        roomyId = sessionRepository.save(Session.builder().name("Main Hall").capacity(100).build()).getId();
        tinySessionId = sessionRepository.save(Session.builder().name("Small Room").capacity(2).build()).getId();
    }

    @Test
    void registersNewAttendeeSuccessfully() {
        var response = attendeeService.register(
                new RegisterAttendeeRequest("Jane Doe", "jane@example.com", roomyId, false));

        assertNotNull(response.getId());
        assertNotNull(response.getTicketId());
        assertEquals("Jane Doe", response.getFullName());
        assertFalse(response.isCheckedIn());
    }

    @Test
    void rejectsDuplicateEmailRegistration() {
        attendeeService.register(new RegisterAttendeeRequest("Jane Doe", "jane@example.com", roomyId, false));

        assertThrows(DuplicateRegistrationException.class, () ->
                attendeeService.register(new RegisterAttendeeRequest("Jane D.", "jane@example.com", roomyId, false)));
    }

    @Test
    void checksInRegisteredAttendeeById() {
        var registered = attendeeService.register(
                new RegisterAttendeeRequest("Jane Doe", "jane@example.com", roomyId, false));

        var response = attendeeService.checkIn(registered.getId());

        assertTrue(response.isCheckedIn());
        assertNotNull(response.getCheckInTime());
    }

    @Test
    void throwsWhenCheckingInUnknownAttendee() {
        assertThrows(AttendeeNotFoundException.class, () -> attendeeService.checkIn(999L));
    }

    @Test
    void reCheckingInAlreadyCheckedInAttendeeIsIdempotent() {
        var registered = attendeeService.register(
                new RegisterAttendeeRequest("Jane Doe", "jane@example.com", roomyId, false));
        attendeeService.checkIn(registered.getId());

        // Second scan of the same badge should not throw or double-count.
        var response = attendeeService.checkIn(registered.getId());
        assertTrue(response.isCheckedIn());

        var dashboard = attendeeService.getDashboard();
        assertEquals(1, dashboard.getTotalCheckedIn());
    }

    @Test
    void undoingCheckInReturnsAttendeeToRegistered() {
        var registered = attendeeService.register(
                new RegisterAttendeeRequest("Jane Doe", "jane@example.com", roomyId, false));
        attendeeService.checkIn(registered.getId());

        var response = attendeeService.undoCheckIn(registered.getId());

        assertFalse(response.isCheckedIn());
        assertNull(response.getCheckInTime());
    }

    @Test
    void rejectsCheckInOnceSessionIsAtCapacity() {
        var first = attendeeService.register(
                new RegisterAttendeeRequest("Person One", "one@example.com", tinySessionId, false));
        var second = attendeeService.register(
                new RegisterAttendeeRequest("Person Two", "two@example.com", tinySessionId, false));
        var third = attendeeService.register(
                new RegisterAttendeeRequest("Person Three", "three@example.com", tinySessionId, false));

        attendeeService.checkIn(first.getId());
        attendeeService.checkIn(second.getId());

        assertThrows(CapacityExceededException.class, () -> attendeeService.checkIn(third.getId()));
    }

    @Test
    void dashboardReflectsCapacityCorrectly() {
        var one = attendeeService.register(new RegisterAttendeeRequest("Person One", "one@example.com", roomyId, false));
        var two = attendeeService.register(new RegisterAttendeeRequest("Person Two", "two@example.com", roomyId, false));
        attendeeService.checkIn(one.getId());
        attendeeService.checkIn(two.getId());

        var dashboard = attendeeService.getDashboard();

        assertEquals(2, dashboard.getTotalCheckedIn());
        assertEquals(0, dashboard.getSpotsRemaining());
        assertTrue(dashboard.isCapacityReached());
    }
}
