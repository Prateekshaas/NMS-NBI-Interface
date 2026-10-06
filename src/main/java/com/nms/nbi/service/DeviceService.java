package com.nms.nbi.service;

import com.nms.nbi.exception.DeviceNotFoundException;
import com.nms.nbi.model.Device;
import com.nms.nbi.model.DeviceConfig;
import com.nms.nbi.model.DeviceStatus;
import com.nms.nbi.model.DeviceType;
import com.nms.nbi.model.MetricSnapshot;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.springframework.stereotype.Service;

/** In-memory device inventory + metric history (stands in for a real southbound/database layer). */
@Service
public class DeviceService {

    private static final int MAX_HISTORY = 60;

    private final HealthEvaluator health;
    private final Map<String, Device> devices = new LinkedHashMap<>();
    private final Map<String, Deque<MetricSnapshot>> history = new LinkedHashMap<>();

    public DeviceService(HealthEvaluator health) {
        this.health = health;
    }

    @PostConstruct
    void seed() {
        add("R1", "Core-Router-1", DeviceType.ROUTER, "192.168.1.1", 42, 61, 48, 0.2, 12);
        add("R2", "Core-Router-2", DeviceType.ROUTER, "192.168.1.2", 38, 55, 46, 0.1, 10);
        add("SW1", "Access-Switch-1", DeviceType.SWITCH, "192.168.1.11", 35, 50, 44, 0.1, 6);
        add("SW2", "Access-Switch-2", DeviceType.SWITCH, "192.168.1.12", 82, 76, 55, 1.8, 95);
        add("AP1", "Lobby-AP-1", DeviceType.ACCESS_POINT, "192.168.1.21", 0, 0, 0, 0, 0);
        devices.get("AP1").setStatus(DeviceStatus.OFFLINE);
    }

    private void add(String id, String name, DeviceType type, String ip,
                     double cpu, double mem, double temp, double loss, double latency) {
        DeviceConfig cfg = new DeviceConfig(name.toLowerCase(), "pool.ntp.org", true, "INFO");
        Device d = new Device(id, name, type, ip, cfg);
        d.setMetrics(cpu, mem, temp, loss, latency);
        d.setStatus(DeviceStatus.ONLINE);
        d.setStatus(health.statusFor(d));
        devices.put(id, d);
        history.put(id, new ArrayDeque<>());
        record(d);
    }

    public synchronized List<Device> findAll() {
        return devices.values().stream().sorted(Comparator.comparing(Device::getId)).toList();
    }

    public synchronized Device get(String id) {
        Device d = devices.get(id);
        if (d == null) throw new DeviceNotFoundException(id);
        return d;
    }

    public synchronized Device updateConfig(String id, DeviceConfig config) {
        Device d = get(id);
        d.setConfig(config);
        return d;
    }

    /** Simulated restart: device comes back ONLINE with healthy baseline metrics. */
    public synchronized Device restart(String id) {
        Device d = get(id);
        d.setStatus(DeviceStatus.ONLINE);
        d.setMetrics(20, 35, 40, 0.1, 8);
        d.setStatus(health.statusFor(d));
        record(d);
        return d;
    }

    public synchronized List<MetricSnapshot> history(String id) {
        get(id); // 404 if unknown
        return List.copyOf(history.get(id));
    }

    /** Called by the simulator: random-walk the metrics of every reachable device. */
    public synchronized void tick(Random rnd) {
        for (Device d : devices.values()) {
            if (d.getStatus() == DeviceStatus.OFFLINE) continue;
            d.setMetrics(
                    drift(d.getCpuUsage(), rnd, 4, 5, 99),
                    drift(d.getMemoryUsage(), rnd, 2, 10, 99),
                    drift(d.getTemperature(), rnd, 1, 30, 90),
                    drift(d.getPacketLoss(), rnd, 0.2, 0, 15),
                    drift(d.getLatency(), rnd, 3, 1, 300));
            d.setStatus(DeviceStatus.ONLINE);
            d.setStatus(health.statusFor(d));
            record(d);
        }
    }

    private double drift(double v, Random rnd, double step, double min, double max) {
        double next = v + rnd.nextGaussian() * step;
        next = Math.max(min, Math.min(max, next));
        return Math.round(next * 10.0) / 10.0;
    }

    private void record(Device d) {
        Deque<MetricSnapshot> q = history.get(d.getId());
        q.addLast(new MetricSnapshot(Instant.now(), d.getCpuUsage(), d.getMemoryUsage(),
                d.getTemperature(), d.getPacketLoss(), d.getLatency()));
        while (q.size() > MAX_HISTORY) q.removeFirst();
    }
}
