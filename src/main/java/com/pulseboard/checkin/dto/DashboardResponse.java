package com.pulseboard.checkin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class DashboardResponse {

    private int capacity;
    private long totalRegistered;
    private long totalCheckedIn;
    private long spotsRemaining;
    private long vipCount;
    private long vipCheckedInCount;
    private List<AttendeeResponse> recentCheckIns;
    private List<SessionResponse> sessions;
    private boolean capacityReached;
}
