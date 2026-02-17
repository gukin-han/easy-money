package com.easymoney.disclosure.application.service;

import com.easymoney.disclosure.domain.model.Disclosure;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DisclosureCollectionService {

    private final DisclosureCollector collector;
    private final DisclosureProcessor processor;

    @Transactional
    public int collect() {
        List<Disclosure> newDisclosures = collector.fetchNew();
        processor.processAll(newDisclosures);
        return newDisclosures.size();
    }
}
