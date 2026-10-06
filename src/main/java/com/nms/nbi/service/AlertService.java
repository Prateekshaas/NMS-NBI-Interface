package com.nms.nbi.service;

import com.nms.nbi.model.Alert;
import com.nms.nbi.model.Severity;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AlertService {

    private final DeviceService devices;
    private final HealthEvaluator health;

    public AlertService(DeviceService devices, HealthEvaluator health) {
        this.devices = devices;
        this.health = health;
    }

    /** Alerts are computed from current device state; both filters are optional. */
    public List<Alert> current(Severity severity, String deviceId) {
        return devices.findAll().stream()
                .filter(d -> deviceId == null || d.getId().equalsIgnoreCase(deviceId))
                .flatMap(d -> health.evaluate(d).stream())
                .filter(a -> severity == null || a.severity() == severity)
                .toList();
    }
}
