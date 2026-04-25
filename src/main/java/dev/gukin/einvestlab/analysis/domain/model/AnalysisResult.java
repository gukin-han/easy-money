package dev.gukin.einvestlab.analysis.domain.model;

public record AnalysisResult(
        Sentiment sentiment,
        int score,
        String summary
) {
}
