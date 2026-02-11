package com.easymoney.disclosure.infrastructure.api;

import com.easymoney.disclosure.domain.model.Disclosure;
import com.easymoney.disclosure.domain.repository.DartClient;
import com.easymoney.disclosure.infrastructure.api.dto.DartDisclosureItem;
import com.easymoney.disclosure.infrastructure.api.dto.DartDisclosureListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DartApiClient implements DartClient {

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
            return List.of();
        }

        return response.list().stream()
                .map(this::toDisclosure)
                .toList();
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
