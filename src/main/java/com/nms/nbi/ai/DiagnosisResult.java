package com.nms.nbi.ai;

import java.util.List;

public record DiagnosisResult(
        String deviceId,
        String predictedIssue,
        String riskLevel,
        double anomalyScore,
        double confidence,
        List<String> contributingMetrics,
        String recommendedAction
) {
}
