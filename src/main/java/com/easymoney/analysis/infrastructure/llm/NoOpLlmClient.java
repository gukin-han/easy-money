package com.easymoney.analysis.infrastructure.llm;

import com.easymoney.analysis.domain.model.AnalysisResult;
import com.easymoney.analysis.domain.model.Sentiment;
import com.easymoney.analysis.domain.repository.LlmClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NoOpLlmClient implements LlmClient {

    @Override
    public AnalysisResult analyze(String corporateName, String title, String content) {
        log.warn("LLM 미설정 — 분석 건너뜀: {} - {}", corporateName, title);
        return new AnalysisResult(Sentiment.NEUTRAL, 0, "LLM 미설정으로 분석을 수행하지 못했습니다.");
    }
}
