package com.nms.nbi.controller;

import com.nms.nbi.model.Alert;
import com.nms.nbi.model.Severity;
import com.nms.nbi.service.AlertService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService service;

    public AlertController(AlertService service) {
        this.service = service;
    }

    /** Optional filters: /api/alerts?severity=CRITICAL&deviceId=SW2 */
    @GetMapping
    public List<Alert> list(@RequestParam(required = false) Severity severity,
                            @RequestParam(required = false) String deviceId) {
        return service.current(severity, deviceId);
    }
}
