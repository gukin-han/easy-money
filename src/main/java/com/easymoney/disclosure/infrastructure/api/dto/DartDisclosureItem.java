package com.easymoney.disclosure.infrastructure.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DartDisclosureItem(
        @JsonProperty("rcept_no") String receiptNumber,
        @JsonProperty("corp_name") String corporateName,
        @JsonProperty("stock_code") String stockCode,
        @JsonProperty("report_nm") String reportName,
        @JsonProperty("rcept_dt") String receiptDate
) {
}
