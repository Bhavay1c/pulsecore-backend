package com.pulseboard.checkin;

import com.pulseboard.checkin.dto.CheckInRequest;
import com.pulseboard.checkin.dto.RegisterAttendeeRequest;
import com.pulseboard.checkin.exception.AttendeeNotFoundException;
import com.pulseboard.checkin.exception.DuplicateRegistrationException;
import com.pulseboard.checkin.repository.AttendeeRepository;
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

    @BeforeEach
    void cleanSlate() {
        attendeeRepository.deleteAll();
        // Keep capacity small and predictable for the capacity test below.
        ReflectionTestUtils.setField(attendeeService, "capacity", 2);
    }

    @Test
    void registersNewAttendeeSuccessfully() {
        var response = attendeeService.register(
                new RegisterAttendeeRequest("Jane Doe", "jane@example.com", false));

        assertNotNull(response.getId());
        assertEquals("Jane Doe", response.getFullName());
        assertFalse(response.isCheckedIn());
    }

    @Test
    void rejectsDuplicateEmailRegistration() {
        attendeeService.register(new RegisterAttendeeRequest("Jane Doe", "jane@example.com", false));

        assertThrows(DuplicateRegistrationException.class, () ->
                attendeeService.register(new RegisterAttendeeRequest("Jane D.", "jane@example.com", false)));
    }

    @Test
    void checksInRegisteredAttendeeByName() {
        attendeeService.register(new RegisterAttendeeRequest("Jane Doe", "jane@example.com", false));

        var response = attendeeService.checkIn(new CheckInRequest("Jane Doe"));

        assertTrue(response.isCheckedIn());
        assertNotNull(response.getCheckInTime());
    }

    @Test
    void checkInIsCaseInsensitive() {
        attendeeService.register(new RegisterAttendeeRequest("Jane Doe", "jane@example.com", false));

        var response = attendeeService.checkIn(new CheckInRequest("jane doe"));

        assertTrue(response.isCheckedIn());
    }

    @Test
    void throwsWhenCheckingInUnknownAttendee() {
        assertThrows(AttendeeNotFoundException.class, () ->
                attendeeService.checkIn(new CheckInRequest("Nobody Here")));
    }

    @Test
    void reCheckingInAlreadyCheckedInAttendeeIsIdempotent() {
        attendeeService.register(new RegisterAttendeeRequest("Jane Doe", "jane@example.com", false));
        attendeeService.checkIn(new CheckInRequest("Jane Doe"));

        // Second scan of the same badge should not throw or double-count.
        var response = attendeeService.checkIn(new CheckInRequest("Jane Doe"));
        assertTrue(response.isCheckedIn());

        var dashboard = attendeeService.getDashboard();
        assertEquals(1, dashboard.getTotalCheckedIn());
    }

    @Test
    void dashboardReflectsCapacityCorrectly() {
        attendeeService.register(new RegisterAttendeeRequest("Person One", "one@example.com", false));
        attendeeService.register(new RegisterAttendeeRequest("Person Two", "two@example.com", false));
        attendeeService.checkIn(new CheckInRequest("Person One"));
        attendeeService.checkIn(new CheckInRequest("Person Two"));

        var dashboard = attendeeService.getDashboard();

        assertEquals(2, dashboard.getTotalCheckedIn());
        assertEquals(0, dashboard.getSpotsRemaining());
        assertTrue(dashboard.isCapacityReached());
    }
}
