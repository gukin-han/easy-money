package com.easymoney.disclosure.interfaces.scheduler;

import com.easymoney.disclosure.application.service.DisclosureCollectionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DartPollingSchedulerTest {

    @Mock
    private DisclosureCollectionService disclosureCollectionService;

    @InjectMocks
    private DartPollingScheduler scheduler;

    @Test
    void poll_호출시_collect를_실행한다() {
        given(disclosureCollectionService.collect()).willReturn(3);

        scheduler.poll();

        verify(disclosureCollectionService).collect();
    }

    @Test
    void collect_예외가_발생해도_전파하지_않는다() {
        given(disclosureCollectionService.collect()).willThrow(new RuntimeException("API 오류"));

        scheduler.poll();

        verify(disclosureCollectionService).collect();
    }
}
