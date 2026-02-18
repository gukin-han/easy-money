package com.easymoney.integration;

import com.easymoney.analysis.application.service.AnalysisService;
import com.easymoney.analysis.domain.model.AnalysisReport;
import com.easymoney.analysis.domain.model.Sentiment;
import com.easymoney.analysis.infrastructure.persistence.JpaAnalysisReportRepository;
import com.easymoney.disclosure.domain.model.Disclosure;
import com.easymoney.disclosure.domain.repository.DartClient;
import com.easymoney.disclosure.infrastructure.persistence.JpaDisclosureRepository;
import com.easymoney.market.domain.repository.StockClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.easymoney.support.MySqlTestContainerConfig;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

@SpringBootTest
@Import(MySqlTestContainerConfig.class)
@ActiveProfiles("local")
@EnabledIf("isOpenAiKeyAvailable")
class LlmAnalysisIntegrationTest {

    static boolean isOpenAiKeyAvailable() {
        String envKey = System.getenv("OPENAI_API_KEY");
        if (envKey != null && !envKey.isBlank()) return true;

        try (InputStream is = LlmAnalysisIntegrationTest.class
                .getClassLoader().getResourceAsStream("application-local.properties")) {
            if (is == null) return false;
            Properties props = new Properties();
            props.load(is);
            String key = props.getProperty("spring.ai.openai.api-key", "");
            return !key.isBlank();
        } catch (Exception e) {
            return false;
        }
    }

    @Autowired
    private AnalysisService analysisService;

    @Autowired
    private DartClient dartClient;

    @Autowired
    private JpaDisclosureRepository disclosureRepository;

    @Autowired
    private JpaAnalysisReportRepository analysisReportRepository;

    @MockitoBean
    private StockClient stockClient;

    @AfterEach
    void tearDown() {
        analysisReportRepository.deleteAll();
        disclosureRepository.deleteAll();
    }

    @Test
    @DisplayName("삼성전자 자기주식취득결과보고서를 실제 LLM으로 분석한다")
    void shouldAnalyzeSamsungTreasuryStockReportWithLlm() {
        analyzeRealDartDisclosureWithLlm(
                "20250217001961", "삼성전자", "005930", "자기주식취득결과보고서"
        );
    }

    @Test
    @DisplayName("STX 매출액변경 공시를 실제 LLM으로 분석한다")
    void shouldAnalyzeStxRevenueChangeDisclosureWithLlm() {
        analyzeRealDartDisclosureWithLlm(
                "20250217801249", "STX", "011810", "매출액또는손익구조30%(대규모법인은15%)이상변경"
        );
    }

    private void analyzeRealDartDisclosureWithLlm(
            String receiptNumber, String corporateName, String stockCode, String title) {
        // given — DART에서 실제 공시 본문을 가져온다
        String content = dartClient.fetchDocumentContent(receiptNumber);
        assumeThat(content).as("DART에서 공시 본문을 가져올 수 없음: " + receiptNumber).isNotBlank();

        Disclosure disclosure = disclosureRepository.save(Disclosure.builder()
                .receiptNumber(receiptNumber)
                .corporateName(corporateName)
                .stockCode(stockCode)
                .title(title)
                .disclosedAt(LocalDateTime.of(2025, 2, 17, 16, 0))
                .build());

        // when — 실제 LLM으로 분석
        analysisService.analyze(
                disclosure.getId(), receiptNumber, corporateName, title, content
        );

        // then — 형식만 검증 (LLM 응답은 비결정적)
        AnalysisReport report = analysisReportRepository
                .findByDisclosureId(disclosure.getId()).orElseThrow();

        assertThat(report.getSentiment()).isIn(Sentiment.POSITIVE, Sentiment.NEUTRAL, Sentiment.NEGATIVE);
        assertThat(report.getScore()).isBetween(-100, 100);
        assertThat(report.getSummary()).isNotBlank();
        assertThat(report.getCorporateName()).isEqualTo(corporateName);
    }
}
