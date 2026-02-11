package com.easymoney.disclosure.infrastructure.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DartDisclosureListResponse(
        String status,
        String message,
        @JsonProperty("page_no") Integer pageNo,
        @JsonProperty("page_count") Integer pageCount,
        @JsonProperty("total_count") Integer totalCount,
        @JsonProperty("total_page") Integer totalPage,
        List<DartDisclosureItem> list
) {
}
