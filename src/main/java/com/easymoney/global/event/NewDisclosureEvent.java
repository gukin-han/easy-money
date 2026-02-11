package com.easymoney.global.event;

public record NewDisclosureEvent(
        Long disclosureId,
        String receiptNumber,
        String corporateName,
        String title
) {
}
