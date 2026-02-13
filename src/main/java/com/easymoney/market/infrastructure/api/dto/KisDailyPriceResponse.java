package com.easymoney.market.infrastructure.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record KisDailyPriceResponse(
        @JsonProperty("rt_cd") String returnCode,
        @JsonProperty("msg_cd") String messageCode,
        @JsonProperty("msg1") String message,
        @JsonProperty("output2") List<KisDailyPriceItem> items
) {
}
