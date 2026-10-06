package com.nms.nbi.ai;

import java.util.List;

public record ExplanationResult(
        String deviceId,
        String explanation,
        List<String> signals
) {
}
