package com.easymoney.analysis.infrastructure.llm;

import com.easymoney.analysis.domain.model.AnalysisResult;
import com.easymoney.analysis.domain.repository.LlmClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Primary
@ConditionalOnProperty(name = "spring.ai.openai.api-key")
@RequiredArgsConstructor
public class SpringAiLlmClient implements LlmClient {

    private final ChatClient chatClient;

    @Override
    public AnalysisResult analyze(String corporateName, String title, String content) {
        String prompt = """
                다음 공시 정보를 분석하여 투자자 관점에서 감성 분석을 수행하세요.

                기업명: %s
                공시 제목: %s
                공시 본문:
                %s

                다음 형식으로 응답하세요:
                - sentiment: POSITIVE, NEUTRAL, NEGATIVE 중 하나
                - score: -100 ~ +100 사이의 정수 (호재일수록 높은 점수)
                - summary: 한국어로 2-3문장 요약
                """.formatted(corporateName, title, content);

        log.info("LLM 프롬프트:\n{}", prompt);

        AnalysisResult result = chatClient.prompt()
                .user(prompt)
                .call()
                .entity(AnalysisResult.class);

        log.info("LLM 응답: {}", result);

        return result;
    }
}
