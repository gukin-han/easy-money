package com.easymoney.disclosure.infrastructure.api;

import com.easymoney.disclosure.domain.model.Disclosure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class DartApiClientTest {

    private MockRestServiceServer mockServer;
    private DartApiClient dartApiClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://opendart.fss.or.kr/api");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        dartApiClient = new DartApiClient(builder.build());
        ReflectionTestUtils.setField(dartApiClient, "apiKey", "test-key");
    }

    @Test
    void 응답을_Disclosure로_변환한다() {
        String responseBody = """
                {
                    "status": "000",
                    "message": "정상",
                    "page_no": 1,
                    "page_count": 100,
                    "total_count": 1,
                    "total_page": 1,
                    "list": [
                        {
                            "rcept_no": "20240515000001",
                            "corp_name": "삼성전자",
                            "stock_code": "005930",
                            "report_nm": "사업보고서",
                            "rcept_dt": "20240515"
                        }
                    ]
                }
                """;

        mockServer.expect(requestTo("https://opendart.fss.or.kr/api/list.json?crtfc_key=test-key&page_count=100"))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        List<Disclosure> result = dartApiClient.fetchRecentDisclosures();

        assertThat(result).hasSize(1);
        Disclosure d = result.getFirst();
        assertThat(d.getReceiptNumber()).isEqualTo("20240515000001");
        assertThat(d.getCorporateName()).isEqualTo("삼성전자");
        assertThat(d.getStockCode()).isEqualTo("005930");
        assertThat(d.getTitle()).isEqualTo("사업보고서");
        assertThat(d.getDisclosedAt()).isEqualTo(LocalDateTime.of(2024, 5, 15, 0, 0));
        assertThat(d.getDocumentUrl()).isEqualTo("https://dart.fss.or.kr/dsaf001/main.do?rcpNo=20240515000001");

        mockServer.verify();
    }

    @Test
    void 빈_응답이면_빈_리스트를_반환한다() {
        String responseBody = """
                {
                    "status": "013",
                    "message": "조회된 데이터가 없습니다."
                }
                """;

        mockServer.expect(requestTo("https://opendart.fss.or.kr/api/list.json?crtfc_key=test-key&page_count=100"))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        List<Disclosure> result = dartApiClient.fetchRecentDisclosures();

        assertThat(result).isEmpty();
        mockServer.verify();
    }
}
