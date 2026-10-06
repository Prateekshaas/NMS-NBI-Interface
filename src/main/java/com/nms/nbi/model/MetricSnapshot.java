package com.nms.nbi.model;

import java.time.Instant;

/** One point in a device's metric history (this is the data the AI layer will train on later). */
public record MetricSnapshot(
        Instant timestamp,
        double cpuUsage,
        double memoryUsage,
        double temperature,
        double packetLoss,
        double latency
) { }
