package dev.gukin.einvestlab.analysis.domain.repository;

import dev.gukin.einvestlab.analysis.domain.model.AnalysisResult;

public interface LlmClient {

    AnalysisResult analyze(String corporateName, String title, String content);
}
