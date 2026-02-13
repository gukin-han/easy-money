package com.easymoney.market.infrastructure.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KisStockPriceResponse(
        @JsonProperty("rt_cd") String returnCode,
        @JsonProperty("msg_cd") String messageCode,
        @JsonProperty("msg1") String message,
        @JsonProperty("output") KisStockPriceOutput output
) {
}
