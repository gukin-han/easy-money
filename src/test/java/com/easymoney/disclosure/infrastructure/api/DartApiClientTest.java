package com.easymoney.disclosure.infrastructure.api;

import com.easymoney.disclosure.domain.model.Disclosure;
import com.easymoney.global.error.DartApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @ParameterizedTest
    @CsvSource({
            "010, 등록되지 않은 인증키입니다.",
            "011, 사용량이 초과되었습니다.",
            "020, 요청 파라미터가 잘못되었습니다.",
            "800, 시스템 점검 중입니다.",
            "900, 정의되지 않은 오류가 발생했습니다."
    })
    void 에러_status이면_DartApiException을_던진다(String status, String message) {
        String responseBody = """
                {
                    "status": "%s",
                    "message": "%s"
                }
                """.formatted(status, message);

        mockServer.expect(requestTo("https://opendart.fss.or.kr/api/list.json?crtfc_key=test-key&page_count=100"))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> dartApiClient.fetchRecentDisclosures())
                .isInstanceOf(DartApiException.class)
                .satisfies(e -> {
                    DartApiException ex = (DartApiException) e;
                    assertThat(ex.getStatus()).isEqualTo(status);
                    assertThat(ex.getMessage()).isEqualTo(message);
                });

        mockServer.verify();
    }

    @Test
    void fetchDocumentContent_ZIP에서_XML_텍스트를_추출한다() throws IOException {
        byte[] zipBytes = createZipWithXml("<root><body>공시 본문 내용입니다</body></root>");

        mockServer.expect(requestTo("https://opendart.fss.or.kr/api/document.xml?crtfc_key=test-key&rcept_no=20240515000001"))
                .andRespond(withSuccess(zipBytes, MediaType.APPLICATION_OCTET_STREAM));

        String content = dartApiClient.fetchDocumentContent("20240515000001");

        assertThat(content).isEqualTo("공시 본문 내용입니다");
        mockServer.verify();
    }

    @Test
    void fetchDocumentContent_10000자를_초과하면_잘라낸다() throws IOException {
        String longText = "가".repeat(15_000);
        byte[] zipBytes = createZipWithXml("<root>" + longText + "</root>");

        mockServer.expect(requestTo("https://opendart.fss.or.kr/api/document.xml?crtfc_key=test-key&rcept_no=001"))
                .andRespond(withSuccess(zipBytes, MediaType.APPLICATION_OCTET_STREAM));

        String content = dartApiClient.fetchDocumentContent("001");

        assertThat(content).hasSize(10_000);
        mockServer.verify();
    }

    @Test
    void fetchDocumentContent_실패시_빈_문자열을_반환한다() {
        mockServer.expect(requestTo("https://opendart.fss.or.kr/api/document.xml?crtfc_key=test-key&rcept_no=001"))
                .andRespond(withSuccess(new byte[0], MediaType.APPLICATION_OCTET_STREAM));

        String content = dartApiClient.fetchDocumentContent("001");

        assertThat(content).isEmpty();
        mockServer.verify();
    }

    private byte[] createZipWithXml(String xmlContent) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            zos.putNextEntry(new ZipEntry("document.xml"));
            zos.write(xmlContent.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }
        return baos.toByteArray();
    }
}
