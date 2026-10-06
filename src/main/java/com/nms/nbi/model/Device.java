package com.nms.nbi.model;

import java.time.Instant;

/** A managed network element together with its latest metrics. */
public class Device {
    private final String id;
    private final DeviceType type;
    private final String ip;
    private String name;
    private DeviceStatus status;
    private double cpuUsage;       // %
    private double memoryUsage;    // %
    private double temperature;    // degrees C
    private double packetLoss;     // %
    private double latency;        // ms
    private DeviceConfig config;
    private Instant lastUpdated = Instant.now();

    public Device(String id, String name, DeviceType type, String ip, DeviceConfig config) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.ip = ip;
        this.config = config;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public DeviceType getType() { return type; }
    public String getIp() { return ip; }
    public DeviceStatus getStatus() { return status; }
    public double getCpuUsage() { return cpuUsage; }
    public double getMemoryUsage() { return memoryUsage; }
    public double getTemperature() { return temperature; }
    public double getPacketLoss() { return packetLoss; }
    public double getLatency() { return latency; }
    public DeviceConfig getConfig() { return config; }
    public Instant getLastUpdated() { return lastUpdated; }

    public void setName(String name) { this.name = name; }
    public void setStatus(DeviceStatus status) { this.status = status; }
    public void setConfig(DeviceConfig config) { this.config = config; }

    public void setMetrics(double cpu, double mem, double temp, double loss, double latency) {
        this.cpuUsage = cpu;
        this.memoryUsage = mem;
        this.temperature = temp;
        this.packetLoss = loss;
        this.latency = latency;
        this.lastUpdated = Instant.now();
    }
}
