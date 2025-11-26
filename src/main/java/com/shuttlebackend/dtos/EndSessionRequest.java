package com.shuttlebackend.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EndSessionRequest {
    private Integer sessionId; // optional: if null, end the current active session for the authenticated driver
}

