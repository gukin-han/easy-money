package com.easymoney.disclosure.interfaces.scheduler;

import com.easymoney.disclosure.application.service.DisclosureCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DartPollingScheduler {

    private final DisclosureCollectionService disclosureCollectionService;

    @Scheduled(fixedDelayString = "${dart.polling.interval:60000}")
    public void poll() {
        try {
            int count = disclosureCollectionService.collect();
            if (count > 0) {
                log.info("새 공시 {}건 수집 완료", count);
            }
        } catch (Exception e) {
            log.error("공시 수집 실패", e);
        }
    }
}
