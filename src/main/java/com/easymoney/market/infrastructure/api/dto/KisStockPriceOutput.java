package com.easymoney.market.infrastructure.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KisStockPriceOutput(
        @JsonProperty("stck_oprc") String openPrice,
        @JsonProperty("stck_hgpr") String highPrice,
        @JsonProperty("stck_lwpr") String lowPrice,
        @JsonProperty("stck_prpr") String currentPrice,
        @JsonProperty("acml_vol") String volume
) {
}
