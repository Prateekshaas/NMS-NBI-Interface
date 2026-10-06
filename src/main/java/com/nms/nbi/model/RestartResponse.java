package com.nms.nbi.model;

import java.time.Instant;

public record RestartResponse(String device, DeviceStatus status, String message, Instant timestamp) { }
