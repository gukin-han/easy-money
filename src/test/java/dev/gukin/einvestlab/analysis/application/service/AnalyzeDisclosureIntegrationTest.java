package dev.gukin.einvestlab.analysis.application.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import dev.gukin.einvestlab.analysis.domain.model.AnalysisResult;
import dev.gukin.einvestlab.analysis.domain.model.Sentiment;
import dev.gukin.einvestlab.analysis.domain.repository.LlmClient;
import dev.gukin.einvestlab.analysis.infrastructure.persistence.JpaAnalysisReportRepository;
import dev.gukin.einvestlab.disclosure.domain.repository.DartClient;
import dev.gukin.einvestlab.support.MySqlTestContainerConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Import(MySqlTestContainerConfig.class)
@SpringBootTest
@DisplayName("공시 분석 기능 통합 테스트")
class AnalyzeDisclosureIntegrationTest {

    @MockitoBean
    private DartClient dartClient;

    @MockitoBean
    private LlmClient llmClient;

    @Autowired
    private DisclosureAnalysisFacade sut;

    @Autowired
    private JpaAnalysisReportRepository analysisReportRepository;

    @AfterEach
    void tearDown() {
        analysisReportRepository.deleteAllInBatch();
    }

    @DisplayName("대량의 가상 스레드가 동시에 분석을 실행해도")
    @Nested
    class WhenConcurrentVirtualThreadsAnalyze {

        @DisplayName("커넥션 풀 고갈로 인한 SQLTransientConnectionException이 발생하지 않는다")
        @Test
        void shouldNotExhaustConnectionPool() {
            // given
            given(llmClient.analyze(anyString(), anyString(), anyString()))
                .willAnswer(invocation -> {
                    Thread.sleep(5_000); // 커넥션 보유 중 블로킹
                    return new AnalysisResult(Sentiment.POSITIVE, 0, "summary");
                });
            given(dartClient.fetchDocumentContent(anyString()))
                .willAnswer(invocation -> {
                    Thread.sleep(400); // 커넥션 보유 중 블로킹
                    return "content";
                });

            // when
            ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
            List<? extends Future<?>> results = IntStream.range(0, 700)
                .mapToObj(i -> executor.submit(() ->
                    sut.execute((long) i, "receipt-" + i, "삼성전자", "사업보고서")))
                .toList();

            List<Exception> exceptions = new ArrayList<>();
            results.forEach(result -> {
                    try {
                        result.get();
                    } catch (Exception e) {
                        exceptions.add(e);
                    }
                }
            );

            // then
            Assertions.assertThat(exceptions).isEmpty();
        }
    }
}
