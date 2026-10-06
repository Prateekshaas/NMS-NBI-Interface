package com.nms.nbi.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Writable configuration of a device (what the PUT /config endpoint accepts). */
public record DeviceConfig(
        @NotBlank @Size(max = 63) String hostname,
        @Size(max = 255) String ntpServer,
        @NotNull Boolean snmpEnabled,
        @NotNull @Pattern(regexp = "DEBUG|INFO|WARN|ERROR",
                message = "must be one of DEBUG, INFO, WARN, ERROR") String loggingLevel
) { }
