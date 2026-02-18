package com.easymoney.disclosure.infrastructure.api;

import com.easymoney.disclosure.domain.model.Disclosure;
import com.easymoney.disclosure.domain.repository.DartClient;
import com.easymoney.disclosure.infrastructure.api.dto.DartDisclosureItem;
import com.easymoney.disclosure.infrastructure.api.dto.DartDisclosureListResponse;
import com.easymoney.global.error.DartApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class DartApiClient implements DartClient {

    private static final Set<String> SUCCESS_STATUSES = Set.of("000", "013");

    private final RestClient dartRestClient;

    @Value("${dart.api.key}")
    private String apiKey;

    @Override
    public List<Disclosure> fetchRecentDisclosures() {
        DartDisclosureListResponse response = dartRestClient.get()
                .uri("/list.json?crtfc_key={key}&page_count=100", apiKey)
                .retrieve()
                .body(DartDisclosureListResponse.class);

        if (response == null || response.list() == null) {
            validateStatus(response);
            return List.of();
        }

        validateStatus(response);

        return response.list().stream()
                .filter(item -> item.stockCode() != null && !item.stockCode().isBlank())
                .map(this::toDisclosure)
                .toList();
    }

    private void validateStatus(DartDisclosureListResponse response) {
        if (response == null) {
            return;
        }
        String status = response.status();
        if (status != null && !SUCCESS_STATUSES.contains(status)) {
            throw new DartApiException(status, response.message());
        }
    }

    @Override
    public String fetchDocumentContent(String receiptNumber) {
        try {
            byte[] zipBytes = dartRestClient.get()
                    .uri("/document.xml?crtfc_key={key}&rcept_no={rcpNo}", apiKey, receiptNumber)
                    .retrieve()
                    .body(byte[].class);

            if (zipBytes == null || zipBytes.length == 0) {
                return "";
            }

            return extractTextFromZip(zipBytes);
        } catch (Exception e) {
            log.warn("공시 본문 조회 실패: receiptNumber={}", receiptNumber, e);
            return "";
        }
    }

    private String extractTextFromZip(byte[] zipBytes) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().endsWith(".xml")) {
                    String xml = new String(zis.readAllBytes(), StandardCharsets.UTF_8);
                    String text = xml.replaceAll("<[^>]+>", " ")
                            .replaceAll("\\s+", " ")
                            .trim();
                    if (text.length() > 10_000) {
                        text = text.substring(0, 10_000);
                    }
                    return text;
                }
            }
        }
        return "";
    }

    private Disclosure toDisclosure(DartDisclosureItem item) {
        return Disclosure.builder()
                .receiptNumber(item.receiptNumber())
                .corporateName(item.corporateName())
                .stockCode(item.stockCode())
                .title(item.reportName())
                .disclosedAt(LocalDate.parse(item.receiptDate(), DateTimeFormatter.BASIC_ISO_DATE).atStartOfDay())
                .documentUrl("https://dart.fss.or.kr/dsaf001/main.do?rcpNo=" + item.receiptNumber())
                .build();
    }
}
