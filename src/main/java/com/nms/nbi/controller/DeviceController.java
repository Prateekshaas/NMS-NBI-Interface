package com.nms.nbi.controller;

import com.nms.nbi.model.Device;
import com.nms.nbi.model.DeviceConfig;
import com.nms.nbi.model.MetricSnapshot;
import com.nms.nbi.model.RestartResponse;
import com.nms.nbi.model.StatusResponse;
import com.nms.nbi.service.DeviceService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService service;

    public DeviceController(DeviceService service) {
        this.service = service;
    }

    @GetMapping
    public List<Device> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Device get(@PathVariable String id) {
        return service.get(id);
    }

    @GetMapping("/{id}/status")
    public StatusResponse status(@PathVariable String id) {
        return StatusResponse.from(service.get(id));
    }

    @GetMapping("/{id}/metrics")
    public List<MetricSnapshot> metrics(@PathVariable String id) {
        return service.history(id);
    }

    @PutMapping("/{id}/config")
    public Device updateConfig(@PathVariable String id, @Valid @RequestBody DeviceConfig config) {
        return service.updateConfig(id, config);
    }

    @PostMapping("/{id}/restart")
    public RestartResponse restart(@PathVariable String id) {
        Device d = service.restart(id);
        return new RestartResponse(d.getId(), d.getStatus(),
                d.getName() + " restarted successfully (simulated)", Instant.now());
    }
}
