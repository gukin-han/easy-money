package com.easymoney.disclosure.application.service;

import com.easymoney.disclosure.domain.model.Disclosure;
import com.easymoney.disclosure.domain.model.DisclosureCategory;
import com.easymoney.disclosure.domain.model.DisclosureStatus;
import com.easymoney.disclosure.domain.repository.DisclosureRepository;
import com.easymoney.disclosure.domain.service.DisclosureClassifier;
import com.easymoney.global.event.NewDisclosureEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DisclosureProcessorTest {

    @Mock
    private DisclosureClassifier classifier;

    @Mock
    private DisclosureRepository disclosureRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private DisclosureProcessor processor;

    @Test
    void shouldSaveAndPublishEventForAnalyzableDisclosure() {
        Disclosure d1 = createDisclosure("001", "사업보고서");

        given(classifier.classify("사업보고서")).willReturn(DisclosureCategory.REGULAR_REPORT);
        given(disclosureRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        processor.processAll(List.of(d1));

        assertThat(d1.getStatus()).isEqualTo(DisclosureStatus.PENDING_ANALYSIS);
        verify(disclosureRepository).save(d1);

        ArgumentCaptor<NewDisclosureEvent> captor = ArgumentCaptor.forClass(NewDisclosureEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());

        NewDisclosureEvent event = captor.getValue();
        assertThat(event.receiptNumber()).isEqualTo("001");
        assertThat(event.corporateName()).isEqualTo("테스트회사");
        assertThat(event.stockCode()).isEqualTo("005930");
        assertThat(event.disclosureDate()).isEqualTo(java.time.LocalDate.of(2024, 5, 15));
    }

    @Test
    void shouldSaveButNotPublishEventForIgnoredDisclosure() {
        Disclosure d1 = createDisclosure("001", "[기재정정]사업보고서");

        given(classifier.classify("[기재정정]사업보고서")).willReturn(DisclosureCategory.CORRECTION);
        given(disclosureRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        processor.processAll(List.of(d1));

        assertThat(d1.getStatus()).isEqualTo(DisclosureStatus.IGNORED);
        verify(disclosureRepository).save(d1);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldPublishEventOnlyForAnalyzableFromMixed() {
        Disclosure analyzable = createDisclosure("001", "사업보고서");
        Disclosure ignorable = createDisclosure("002", "[기재정정]사업보고서");

        given(classifier.classify("사업보고서")).willReturn(DisclosureCategory.REGULAR_REPORT);
        given(classifier.classify("[기재정정]사업보고서")).willReturn(DisclosureCategory.CORRECTION);
        given(disclosureRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        processor.processAll(List.of(analyzable, ignorable));

        verify(disclosureRepository).save(analyzable);
        verify(disclosureRepository).save(ignorable);
        verify(eventPublisher, times(1)).publishEvent(any(NewDisclosureEvent.class));
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
