package com.easymoney.disclosure.application.dto;

import com.easymoney.disclosure.domain.model.Disclosure;

import java.time.LocalDateTime;

public record DisclosureInfo(
        Long id,
        String receiptNumber,
        String corporateName,
        String stockCode,
        String title,
        LocalDateTime disclosedAt,
        String documentUrl
) {

    public static DisclosureInfo from(Disclosure disclosure) {
        return new DisclosureInfo(
                disclosure.getId(),
                disclosure.getReceiptNumber(),
                disclosure.getCorporateName(),
                disclosure.getStockCode(),
                disclosure.getTitle(),
                disclosure.getDisclosedAt(),
                disclosure.getDocumentUrl()
        );
    }
}
