package com.nms.nbi.model;

import java.time.Instant;

public record Alert(
        String id,
        String deviceId,
        Severity severity,
        String metric,
        double value,
        String message,
        Instant timestamp
) { }
