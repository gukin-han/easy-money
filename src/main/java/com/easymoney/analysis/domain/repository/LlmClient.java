package com.easymoney.analysis.domain.repository;

import com.easymoney.analysis.domain.model.AnalysisResult;

public interface LlmClient {

    AnalysisResult analyze(String corporateName, String title, String content);
}
