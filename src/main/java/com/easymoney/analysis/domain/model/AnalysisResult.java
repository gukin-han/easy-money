package com.easymoney.analysis.domain.model;

public record AnalysisResult(
        Sentiment sentiment,
        int score,
        String summary
) {
}
