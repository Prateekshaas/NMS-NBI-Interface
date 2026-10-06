package com.nms.nbi.controller;

import com.nms.nbi.ai.DiagnosisResult;
import com.nms.nbi.ai.ExplanationResult;
import com.nms.nbi.service.AiDiagnosisService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiDiagnosisService service;

    public AiController(AiDiagnosisService service) {
        this.service = service;
    }

    @GetMapping("/{id}/anomaly")
    public DiagnosisResult anomaly(@PathVariable String id) {
        return service.diagnose(id);
    }

    @GetMapping("/{id}/failure-risk")
    public DiagnosisResult failureRisk(@PathVariable String id) {
        return service.diagnose(id);
    }

    @GetMapping("/{id}/diagnosis")
    public DiagnosisResult diagnosis(@PathVariable String id) {
        return service.diagnose(id);
    }

    @GetMapping("/{id}/explain")
    public ExplanationResult explain(@PathVariable String id) {
        return service.explain(id);
    }
}
