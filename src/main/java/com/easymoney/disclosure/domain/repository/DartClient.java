package com.easymoney.disclosure.domain.repository;

import com.easymoney.disclosure.domain.model.Disclosure;

import java.time.LocalDate;
import java.util.List;

public interface DartClient {

    List<Disclosure> fetchRecentDisclosures();

    List<Disclosure> fetchDisclosuresByDate(LocalDate date);

    String fetchDocumentContent(String receiptNumber);
}
