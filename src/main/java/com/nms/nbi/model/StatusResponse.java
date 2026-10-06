package com.nms.nbi.model;

import java.time.Instant;

public record StatusResponse(
        String device,
        DeviceStatus status,
        double cpuUsage,
        double memoryUsage,
        double temperature,
        double packetLoss,
        double latency,
        Instant lastUpdated
) {
    public static StatusResponse from(Device d) {
        return new StatusResponse(d.getId(), d.getStatus(), d.getCpuUsage(), d.getMemoryUsage(),
                d.getTemperature(), d.getPacketLoss(), d.getLatency(), d.getLastUpdated());
    }
}
