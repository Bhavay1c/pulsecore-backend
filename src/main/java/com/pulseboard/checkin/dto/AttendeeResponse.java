package com.pulseboard.checkin.dto;

import com.pulseboard.checkin.model.Attendee;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AttendeeResponse {

    private Long id;
    private String fullName;
    private String email;
    private String ticketId;
    private Long sessionId;
    private String sessionName;
    private boolean vip;
    private boolean checkedIn;
    private LocalDateTime checkInTime;
    private LocalDateTime registeredAt;

    public static AttendeeResponse from(Attendee attendee) {
        return new AttendeeResponse(
                attendee.getId(),
                attendee.getFullName(),
                attendee.getEmail(),
                attendee.getTicketId(),
                attendee.getSession().getId(),
                attendee.getSession().getName(),
                attendee.isVip(),
                attendee.isCheckedIn(),
                attendee.getCheckInTime(),
                attendee.getRegisteredAt()
        );
    }
}
