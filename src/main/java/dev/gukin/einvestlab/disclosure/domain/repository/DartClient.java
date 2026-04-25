package dev.gukin.einvestlab.disclosure.domain.repository;

import dev.gukin.einvestlab.disclosure.domain.model.Disclosure;

import java.time.LocalDate;
import java.util.List;

public interface DartClient {

    List<Disclosure> fetchRecentDisclosures();

    List<Disclosure> fetchDisclosuresByDate(LocalDate date);

    String fetchDocumentContent(String receiptNumber);
}
