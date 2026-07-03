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

    @PostMapping("/checkin")
    public AttendeeResponse checkIn(@Valid @RequestBody CheckInRequest request) {
        return attendeeService.checkIn(request);
    }

    @GetMapping("/attendees")
    public List<AttendeeResponse> getAttendees(
            @RequestParam(name = "vip", required = false) Boolean vip) {
        return attendeeService.getAllAttendees(vip);
    }

    @GetMapping("/dashboard")
    public DashboardResponse getDashboard() {
        return attendeeService.getDashboard();
    }
}
