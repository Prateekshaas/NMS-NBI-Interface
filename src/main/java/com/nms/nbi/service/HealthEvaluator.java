package com.nms.nbi.service;

import com.nms.nbi.model.Alert;
import com.nms.nbi.model.Device;
import com.nms.nbi.model.DeviceStatus;
import com.nms.nbi.model.Severity;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/** Rule-based thresholds. One place decides both device status and alerts, so they never disagree. */
@Component
public class HealthEvaluator {

    // metric thresholds: {warning, critical}
    private static final double[] CPU = {75, 90};
    private static final double[] MEMORY = {80, 90};
    private static final double[] TEMP = {60, 70};
    private static final double[] LOSS = {1, 5};
    private static final double[] LATENCY = {80, 150};

    public List<Alert> evaluate(Device d) {
        List<Alert> alerts = new ArrayList<>();
        if (d.getStatus() == DeviceStatus.OFFLINE) {
            alerts.add(new Alert(d.getId() + "-UNREACHABLE", d.getId(), Severity.CRITICAL,
                    "reachability", 0, d.getName() + " is unreachable", Instant.now()));
            return alerts;
        }
        check(alerts, d, "cpuUsage", d.getCpuUsage(), CPU, "%");
        check(alerts, d, "memoryUsage", d.getMemoryUsage(), MEMORY, "%");
        check(alerts, d, "temperature", d.getTemperature(), TEMP, " C");
        check(alerts, d, "packetLoss", d.getPacketLoss(), LOSS, "%");
        check(alerts, d, "latency", d.getLatency(), LATENCY, " ms");
        return alerts;
    }

    /** Status derived from the worst alert; OFFLINE is never overridden here. */
    public DeviceStatus statusFor(Device d) {
        if (d.getStatus() == DeviceStatus.OFFLINE) {
            return DeviceStatus.OFFLINE;
        }
        DeviceStatus result = DeviceStatus.ONLINE;
        for (Alert a : evaluate(d)) {
            if (a.severity() == Severity.CRITICAL) return DeviceStatus.CRITICAL;
            result = DeviceStatus.WARNING;
        }
        return result;
    }

    private void check(List<Alert> out, Device d, String metric, double value, double[] t, String unit) {
        Severity sev = value >= t[1] ? Severity.CRITICAL : value >= t[0] ? Severity.WARNING : null;
        if (sev == null) return;
        double limit = sev == Severity.CRITICAL ? t[1] : t[0];
        out.add(new Alert(d.getId() + "-" + metric.toUpperCase(), d.getId(), sev, metric, value,
                String.format("%s %s is %.1f%s (threshold %.0f%s)", d.getName(), metric, value, unit, limit, unit),
                Instant.now()));
    }
}
