package com.easymoney.disclosure.application.service;

import com.easymoney.disclosure.domain.model.Disclosure;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DisclosureCollectionServiceTest {

    @Mock
    private DisclosureCollector collector;

    @Mock
    private DisclosureProcessor processor;

    @InjectMocks
    private DisclosureCollectionService collectionService;

    @Test
    void shouldCollectThenProcess() {
        List<Disclosure> disclosures = List.of(
                createDisclosure("001", "사업보고서"),
                createDisclosure("002", "사업보고서")
        );
        given(collector.fetchNew()).willReturn(disclosures);

        collectionService.collect();

        InOrder inOrder = inOrder(collector, processor);
        inOrder.verify(collector).fetchNew();
        inOrder.verify(processor).processAll(disclosures);
    }

    @Test
    void shouldReturnCollectedCount() {
        List<Disclosure> disclosures = List.of(
                createDisclosure("001", "사업보고서"),
                createDisclosure("002", "사업보고서"),
                createDisclosure("003", "사업보고서")
        );
        given(collector.fetchNew()).willReturn(disclosures);

        int count = collectionService.collect();

        assertThat(count).isEqualTo(3);
        verify(processor).processAll(disclosures);
    }

    private Disclosure createDisclosure(String receiptNumber, String title) {
        return Disclosure.builder()
                .receiptNumber(receiptNumber)
                .corporateName("테스트회사")
                .stockCode("005930")
                .title(title)
                .disclosedAt(LocalDateTime.of(2024, 5, 15, 0, 0))
                .documentUrl("https://dart.fss.or.kr/dsaf001/main.do?rcpNo=" + receiptNumber)
                .build();
    }
}
