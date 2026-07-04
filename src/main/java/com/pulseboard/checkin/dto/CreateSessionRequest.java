package com.pulseboard.checkin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateSessionRequest {

    @NotBlank(message = "name is required")
    private String name;

    @Min(value = 1, message = "capacity must be at least 1")
    private int capacity;
}
