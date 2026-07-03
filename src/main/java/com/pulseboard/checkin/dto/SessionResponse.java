package com.pulseboard.checkin.dto;

import com.pulseboard.checkin.model.Session;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SessionResponse {

    private Long id;
    private String name;
    private int capacity;
    private long checkedIn;

    public static SessionResponse of(Session session, long checkedIn) {
        return new SessionResponse(session.getId(), session.getName(), session.getCapacity(), checkedIn);
    }
}
