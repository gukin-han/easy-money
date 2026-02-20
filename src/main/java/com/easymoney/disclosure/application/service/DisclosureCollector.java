package com.easymoney.disclosure.application.service;

import com.easymoney.disclosure.domain.model.Disclosure;
import com.easymoney.disclosure.domain.repository.DartClient;
import com.easymoney.disclosure.domain.repository.DisclosureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DisclosureCollector {

    private final DartClient dartClient;
    private final DisclosureRepository disclosureRepository;

    public List<Disclosure> fetchNew() {
        return fetchNewByDate(null);
    }

    public List<Disclosure> fetchNewByDate(LocalDate date) {
        List<Disclosure> fetched = date != null
                ? dartClient.fetchDisclosuresByDate(date)
                : dartClient.fetchRecentDisclosures();
        List<String> receiptNumbers = fetched.stream()
                .map(Disclosure::getReceiptNumber).toList();
        Set<String> existing = disclosureRepository
                .findExistingReceiptNumbers(receiptNumbers);
        return fetched.stream()
                .filter(d -> !existing.contains(d.getReceiptNumber()))
                .toList();
    }
}
