package com.easymoney.integration;

import com.easymoney.analysis.domain.model.AnalysisReport;
import com.easymoney.analysis.domain.model.AnalysisResult;
import com.easymoney.analysis.domain.model.Sentiment;
import com.easymoney.analysis.domain.repository.LlmClient;
import com.easymoney.analysis.infrastructure.persistence.JpaAnalysisReportRepository;
import com.easymoney.disclosure.application.service.DisclosureCollectionService;
import com.easymoney.disclosure.domain.model.Disclosure;
import com.easymoney.disclosure.domain.model.DisclosureCategory;
import com.easymoney.disclosure.domain.model.DisclosureStatus;
import com.easymoney.disclosure.domain.repository.DartClient;
import com.easymoney.disclosure.infrastructure.persistence.JpaDisclosureRepository;
import com.easymoney.market.domain.repository.StockClient;
import com.easymoney.market.infrastructure.persistence.JpaMarketReactionRepository;
import com.easymoney.market.infrastructure.persistence.JpaStockPriceRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.easymoney.support.MySqlTestContainerConfig;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Import(MySqlTestContainerConfig.class)
class DisclosureAnalysisFlowTest {

    @Autowired
    private DisclosureCollectionService collectionService;

    @Autowired
    private JpaDisclosureRepository disclosureRepository;

    @Autowired
    private JpaAnalysisReportRepository analysisReportRepository;

    @Autowired
    private JpaMarketReactionRepository marketReactionRepository;

    @Autowired
    private JpaStockPriceRepository stockPriceRepository;

    @MockitoBean
    private DartClient dartClient;

    @MockitoBean
    private LlmClient llmClient;

    @MockitoBean
    private StockClient stockClient;

    @AfterEach
    void tearDown() {
        analysisReportRepository.deleteAll();
        marketReactionRepository.deleteAll();
        stockPriceRepository.deleteAll();
        disclosureRepository.deleteAll();
    }

    @Test
    @DisplayName("분석 대상 공시 전체 플로우: 수집 → 분류 → 이벤트 → 본문 조회 → LLM 분석 → 저장")
    void shouldRunFullFlowForAnalyzableDisclosure() {
        // given
        Disclosure disclosure = Disclosure.builder()
                .receiptNumber("20240101000001")
                .corporateName("삼성전자")
                .stockCode("005930")
                .title("사업보고서 (2024.12)")
                .disclosedAt(LocalDateTime.of(2024, 3, 15, 16, 30))
                .build();

        given(dartClient.fetchRecentDisclosures()).willReturn(List.of(disclosure));
        given(dartClient.fetchDocumentContent("20240101000001"))
                .willReturn("사업보고서 본문 내용입니다.");
        given(llmClient.analyze("삼성전자", "사업보고서 (2024.12)", "사업보고서 본문 내용입니다."))
                .willReturn(new AnalysisResult(Sentiment.POSITIVE, 75, "매출 및 영업이익 증가로 긍정적입니다."));
        given(stockClient.fetchDailyPrice(anyString(), any())).willReturn(Optional.empty());
        given(stockClient.fetchCurrentPrice(anyString())).willReturn(Optional.empty());

        // when
        collectionService.collect();

        // then
        await().atMost(5, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    Disclosure saved = disclosureRepository.findByReceiptNumber("20240101000001").orElseThrow();
                    assertThat(saved.getStatus()).isEqualTo(DisclosureStatus.ANALYZED);
                });

        Disclosure saved = disclosureRepository.findByReceiptNumber("20240101000001").orElseThrow();
        assertThat(saved.getCategory()).isEqualTo(DisclosureCategory.REGULAR_REPORT);

        Optional<AnalysisReport> report = analysisReportRepository.findByDisclosureId(saved.getId());
        assertThat(report).isPresent();
        assertThat(report.get().getSentiment()).isEqualTo(Sentiment.POSITIVE);
        assertThat(report.get().getScore()).isEqualTo(75);
        assertThat(report.get().getSummary()).isEqualTo("매출 및 영업이익 증가로 긍정적입니다.");
        assertThat(report.get().getCorporateName()).isEqualTo("삼성전자");

        verify(dartClient).fetchDocumentContent("20240101000001");
        verify(llmClient).analyze("삼성전자", "사업보고서 (2024.12)", "사업보고서 본문 내용입니다.");
    }

    @Test
    @DisplayName("분석 제외 공시는 이벤트가 발행되지 않는다")
    void shouldNotPublishEventForIgnoredDisclosure() {
        // given
        Disclosure disclosure = Disclosure.builder()
                .receiptNumber("20240101000002")
                .corporateName("삼성전자")
                .stockCode("005930")
                .title("[기재정정]사업보고서")
                .disclosedAt(LocalDateTime.of(2024, 3, 15, 16, 30))
                .build();

        given(dartClient.fetchRecentDisclosures()).willReturn(List.of(disclosure));

        // when
        collectionService.collect();

        // then — 비동기 이벤트가 발행되지 않으므로 충분히 대기 후 검증
        await().during(500, TimeUnit.MILLISECONDS)
                .atMost(1, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(analysisReportRepository.findAll()).isEmpty());

        Disclosure saved = disclosureRepository.findByReceiptNumber("20240101000002").orElseThrow();
        assertThat(saved.getCategory()).isEqualTo(DisclosureCategory.CORRECTION);
        assertThat(saved.getStatus()).isEqualTo(DisclosureStatus.IGNORED);
        assertThat(saved.getCategory().isAnalyzable()).isFalse();

        verify(dartClient, never()).fetchDocumentContent(anyString());
        verify(llmClient, never()).analyze(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("혼합 공시 중 분석 대상만 분석된다")
    void shouldAnalyzeOnlyAnalyzableFromMixedDisclosures() {
        // given
        Disclosure analyzable = Disclosure.builder()
                .receiptNumber("20240101000003")
                .corporateName("삼성전자")
                .stockCode("005930")
                .title("사업보고서 (2024.12)")
                .disclosedAt(LocalDateTime.of(2024, 3, 15, 16, 30))
                .build();

        Disclosure ignorable = Disclosure.builder()
                .receiptNumber("20240101000004")
                .corporateName("LG전자")
                .stockCode("066570")
                .title("[기재정정]분기보고서")
                .disclosedAt(LocalDateTime.of(2024, 3, 15, 16, 30))
                .build();

        given(dartClient.fetchRecentDisclosures()).willReturn(List.of(analyzable, ignorable));
        given(dartClient.fetchDocumentContent("20240101000003"))
                .willReturn("사업보고서 본문 내용");
        given(llmClient.analyze("삼성전자", "사업보고서 (2024.12)", "사업보고서 본문 내용"))
                .willReturn(new AnalysisResult(Sentiment.NEUTRAL, 10, "특이사항 없음"));
        given(stockClient.fetchDailyPrice(anyString(), any())).willReturn(Optional.empty());
        given(stockClient.fetchCurrentPrice(anyString())).willReturn(Optional.empty());

        // when
        collectionService.collect();

        // then
        await().atMost(5, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    Disclosure saved = disclosureRepository.findByReceiptNumber("20240101000003").orElseThrow();
                    assertThat(saved.getStatus()).isEqualTo(DisclosureStatus.ANALYZED);
                });

        Disclosure analyzed = disclosureRepository.findByReceiptNumber("20240101000003").orElseThrow();
        assertThat(analyzed.getCategory()).isEqualTo(DisclosureCategory.REGULAR_REPORT);

        assertThat(analysisReportRepository.findAll()).hasSize(1);

        Disclosure ignored = disclosureRepository.findByReceiptNumber("20240101000004").orElseThrow();
        assertThat(ignored.getCategory()).isEqualTo(DisclosureCategory.CORRECTION);
        assertThat(ignored.getStatus()).isEqualTo(DisclosureStatus.IGNORED);

        verify(dartClient, never()).fetchDocumentContent("20240101000004");
    }
}
