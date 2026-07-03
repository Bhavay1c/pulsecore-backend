package com.pulseboard.checkin.controller;

import com.pulseboard.checkin.dto.*;
import com.pulseboard.checkin.service.AttendeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AttendeeController {

    private final AttendeeService attendeeService;

    public AttendeeController(AttendeeService attendeeService) {
        this.attendeeService = attendeeService;
    }

    @PostMapping("/attendees/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AttendeeResponse register(@Valid @RequestBody RegisterAttendeeRequest request) {
        return attendeeService.register(request);
    }

    @PostMapping("/attendees/walk-in")
    @ResponseStatus(HttpStatus.CREATED)
    public AttendeeResponse walkIn(@Valid @RequestBody WalkInRequest request) {
        return attendeeService.walkIn(request);
    }

    @PostMapping("/attendees/{id}/checkin")
    public AttendeeResponse checkIn(@PathVariable Long id) {
        return attendeeService.checkIn(id);
    }

    @PostMapping("/attendees/{id}/undo-checkin")
    public AttendeeResponse undoCheckIn(@PathVariable Long id) {
        return attendeeService.undoCheckIn(id);
    }

    @GetMapping("/attendees")
    public List<AttendeeResponse> getAttendees(
            @RequestParam(name = "vip", required = false) Boolean vip) {
        return attendeeService.getAllAttendees(vip);
    }

    @GetMapping("/sessions")
    public List<SessionResponse> getSessions() {
        return attendeeService.getAllSessions();
    }

    @GetMapping("/dashboard")
    public DashboardResponse getDashboard() {
        return attendeeService.getDashboard();
    }
}
