package dev.gukin.einvestlab.global.event;

import java.time.LocalDate;

public record NewDisclosureEvent(
        Long disclosureId,
        String receiptNumber,
        String corporateName,
        String title,
        String stockCode,
        LocalDate disclosureDate
) {
}
