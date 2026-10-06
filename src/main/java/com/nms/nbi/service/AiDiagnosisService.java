package com.nms.nbi.service;

import com.nms.nbi.ai.DiagnosisResult;
import com.nms.nbi.ai.ExplanationResult;
import com.nms.nbi.model.Device;
import com.nms.nbi.model.MetricSnapshot;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class AiDiagnosisService {

    private final DeviceService deviceService;

    public AiDiagnosisService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    public DiagnosisResult diagnose(String deviceId) {
        Device device = deviceService.get(deviceId);
        List<MetricSnapshot> history = deviceService.history(deviceId);

        if (history.isEmpty()) {
            return new DiagnosisResult(
                    deviceId,
                    "Insufficient telemetry",
                    "LOW",
                    0.0,
                    0.0,
                    List.of("No historical baseline available"),
                    "Collect additional telemetry before making a diagnosis."
            );
        }

        Map<String, Double> current = new HashMap<>();
        current.put("cpuUsage", device.getCpuUsage());
        current.put("memoryUsage", device.getMemoryUsage());
        current.put("temperature", device.getTemperature());
        current.put("packetLoss", device.getPacketLoss());
        current.put("latency", device.getLatency());

        Map<String, MetricStats> baseline = new HashMap<>();
        baseline.put("cpuUsage", stats(history.stream().map(MetricSnapshot::cpuUsage).toList()));
        baseline.put("memoryUsage", stats(history.stream().map(MetricSnapshot::memoryUsage).toList()));
        baseline.put("temperature", stats(history.stream().map(MetricSnapshot::temperature).toList()));
        baseline.put("packetLoss", stats(history.stream().map(MetricSnapshot::packetLoss).toList()));
        baseline.put("latency", stats(history.stream().map(MetricSnapshot::latency).toList()));

        Map<String, Double> contributions = new HashMap<>();
        for (String metric : current.keySet()) {
            MetricStats stats = baseline.get(metric);
            double delta = Math.abs(current.get(metric) - stats.mean());
            double deviation = stats.stdDev() > 0 ? delta / stats.stdDev() : 0.0;
            double blend = Math.min(100, Math.max(0, deviation * 20.0 + (current.get(metric) * 0.4)));
            contributions.put(metric, blend);
        }

        String predictedIssue = contributions.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> switch (entry.getKey()) {
                    case "cpuUsage" -> "Sustained CPU saturation";
                    case "memoryUsage" -> "Memory pressure and rising utilization";
                    case "temperature" -> "Thermal overload risk";
                    case "packetLoss" -> "Packet loss and link instability";
                    case "latency" -> "Latency degradation";
                    default -> "Abnormal telemetry trend";
                })
                .orElse("Abnormal telemetry trend");

        double anomalyScore = Math.round(contributions.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0) * 10.0) / 10.0;

        String riskLevel = anomalyScore >= 80 ? "CRITICAL" : anomalyScore >= 60 ? "HIGH" : anomalyScore >= 40 ? "MEDIUM" : "LOW";
        double confidence = Math.min(99.0, 55.0 + (anomalyScore / 2.0));

        List<String> contributingMetrics = contributions.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(3)
                .map(entry -> formatMetric(entry.getKey(), entry.getValue(), current.get(entry.getKey()), baseline.get(entry.getKey()).mean()))
                .collect(Collectors.toList());

        String recommendedAction = switch (predictedIssue) {
            case "Sustained CPU saturation" -> "Inspect the device workload and reduce CPU-intensive traffic or restart the affected process.";
            case "Memory pressure and rising utilization" -> "Check memory-heavy services and look for runaway processes or memory leaks.";
            case "Thermal overload risk" -> "Inspect cooling and airflow, then verify the device is not overheating under load.";
            case "Packet loss and link instability" -> "Examine the uplink path, interface errors, and any link flaps or congestion.";
            case "Latency degradation" -> "Check path congestion, jitter, and upstream network latency before escalating the issue.";
            default -> "Review the latest telemetry and compare it to baseline behavior before taking action.";
        };

        return new DiagnosisResult(
                deviceId,
                predictedIssue,
                riskLevel,
                anomalyScore,
                confidence,
                contributingMetrics,
                recommendedAction
        );
    }

    public ExplanationResult explain(String deviceId) {
        DiagnosisResult diagnosis = diagnose(deviceId);
        Device device = deviceService.get(deviceId);
        List<MetricSnapshot> history = deviceService.history(deviceId);
        if (history.isEmpty()) {
            return new ExplanationResult(deviceId, "No historical telemetry is available for this device.", List.of("Telemetry baseline unavailable"));
        }

        List<String> signals = new ArrayList<>();
        signals.add("Current device state: " + device.getStatus() + ", with " + diagnosis.predictedIssue().toLowerCase() + ".");
        for (String metric : diagnosis.contributingMetrics()) {
            signals.add(metric);
        }

        String explanation = "The current telemetry pattern deviates from the recent baseline for " + deviceId + ". "
                + "The strongest signal is " + diagnosis.predictedIssue().toLowerCase() + ", which indicates a multivariate anomaly rather than a single fixed-threshold failure. "
                + "The system compares recent measurements against historical behavior and identifies the main contributors to the abnormal trend.";

        return new ExplanationResult(deviceId, explanation, signals);
    }

    private String formatMetric(String key, double score, double current, double baseline) {
        String label = switch (key) {
            case "cpuUsage" -> "CPU";
            case "memoryUsage" -> "Memory";
            case "temperature" -> "Temperature";
            case "packetLoss" -> "Packet loss";
            case "latency" -> "Latency";
            default -> key;
        };
        return label + " is " + String.format("%.1f", current) + " vs baseline " + String.format("%.1f", baseline) + " (anomaly score " + String.format("%.1f", score) + ")";
    }

    private MetricStats stats(List<Double> values) {
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0.0);
        return new MetricStats(mean, Math.sqrt(variance));
    }

    private record MetricStats(double mean, double stdDev) {
    }
}
