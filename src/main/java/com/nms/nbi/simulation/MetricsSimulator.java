package com.nms.nbi.simulation;

import com.nms.nbi.service.DeviceService;
import java.util.Random;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Makes the mock network "live" so history and alerts change while you demo. */
@Component
@ConditionalOnProperty(name = "nms.simulation.enabled", havingValue = "true", matchIfMissing = true)
public class MetricsSimulator {

    private final DeviceService devices;
    private final Random rnd = new Random();

    public MetricsSimulator(DeviceService devices) {
        this.devices = devices;
    }

    @Scheduled(fixedRateString = "${nms.simulation.interval-ms:5000}")
    public void tick() {
        devices.tick(rnd);
    }
}
