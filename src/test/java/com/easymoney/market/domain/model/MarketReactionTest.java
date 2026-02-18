package com.easymoney.market.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MarketReactionTest {

    @Test
    void shouldCalculatePositiveChangeRate() {
        BigDecimal rate = MarketReaction.calculateChangeRate(50000, 55000);

        assertThat(rate).isEqualByComparingTo(new BigDecimal("0.1000"));
    }

    @Test
    void shouldCalculateNegativeChangeRate() {
        BigDecimal rate = MarketReaction.calculateChangeRate(50000, 45000);

        assertThat(rate).isEqualByComparingTo(new BigDecimal("-0.1000"));
    }

    @Test
    void shouldReturnZeroWhenNoChange() {
        BigDecimal rate = MarketReaction.calculateChangeRate(50000, 50000);

        assertThat(rate).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldReturnZeroWhenPriorCloseIsZero() {
        BigDecimal rate = MarketReaction.calculateChangeRate(0, 50000);

        assertThat(rate).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldRoundToFourDecimalPlaces() {
        BigDecimal rate = MarketReaction.calculateChangeRate(30000, 31000);

        assertThat(rate).isEqualByComparingTo(new BigDecimal("0.0333"));
    }
}
