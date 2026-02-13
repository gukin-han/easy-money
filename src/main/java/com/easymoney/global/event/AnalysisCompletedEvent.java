package com.easymoney.global.event;

import java.time.LocalDate;

public record AnalysisCompletedEvent(
        Long disclosureId,
        String stockCode,
        String corporateName,
        LocalDate disclosureDate
) {
}
