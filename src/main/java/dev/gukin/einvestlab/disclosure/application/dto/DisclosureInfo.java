package dev.gukin.einvestlab.disclosure.application.dto;

import dev.gukin.einvestlab.disclosure.domain.model.Disclosure;
import dev.gukin.einvestlab.disclosure.domain.model.DisclosureCategory;
import dev.gukin.einvestlab.disclosure.domain.model.DisclosureStatus;

import java.time.LocalDateTime;

public record DisclosureInfo(
        Long id,
        String receiptNumber,
        String corporateName,
        String stockCode,
        String title,
        LocalDateTime disclosedAt,
        String documentUrl,
        DisclosureStatus status,
        DisclosureCategory category,
        LocalDateTime createdAt
) {

    public static DisclosureInfo from(Disclosure disclosure) {
        return new DisclosureInfo(
                disclosure.getId(),
                disclosure.getReceiptNumber(),
                disclosure.getCorporateName(),
                disclosure.getStockCode(),
                disclosure.getTitle(),
                disclosure.getDisclosedAt(),
                disclosure.getDocumentUrl(),
                disclosure.getStatus(),
                disclosure.getCategory(),
                disclosure.getCreatedAt()
        );
    }
}
