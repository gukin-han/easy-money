package com.easymoney.market.application.dto;

import com.easymoney.market.domain.model.MarketReaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MarketReactionInfo(
        Long id,
        Long disclosureId,
        String stockCode,
        long priorClose,
        long currentClose,
        BigDecimal changeRate,
        LocalDateTime trackedAt
) {

    public static MarketReactionInfo from(MarketReaction reaction) {
        return new MarketReactionInfo(
                reaction.getId(),
                reaction.getDisclosureId(),
                reaction.getStockCode(),
                reaction.getPriorClose(),
                reaction.getCurrentClose(),
                reaction.getChangeRate(),
                reaction.getTrackedAt()
        );
    }
}
