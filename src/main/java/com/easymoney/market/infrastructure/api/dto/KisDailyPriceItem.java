package com.easymoney.market.infrastructure.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KisDailyPriceItem(
        @JsonProperty("stck_bsop_date") String tradingDate,
        @JsonProperty("stck_oprc") String openPrice,
        @JsonProperty("stck_hgpr") String highPrice,
        @JsonProperty("stck_lwpr") String lowPrice,
        @JsonProperty("stck_clpr") String closePrice,
        @JsonProperty("acml_vol") String volume
) {
}
