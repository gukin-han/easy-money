package com.easymoney.market.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MarketReactionTest {

    @Test
    void 등락률을_계산한다() {
        BigDecimal rate = MarketReaction.calculateChangeRate(50000, 55000);

        assertThat(rate).isEqualByComparingTo(new BigDecimal("0.1000"));
    }

    @Test
    void 하락률을_계산한다() {
        BigDecimal rate = MarketReaction.calculateChangeRate(50000, 45000);

        assertThat(rate).isEqualByComparingTo(new BigDecimal("-0.1000"));
    }

    @Test
    void 변동없으면_0을_반환한다() {
        BigDecimal rate = MarketReaction.calculateChangeRate(50000, 50000);

        assertThat(rate).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void 전일_종가가_0이면_0을_반환한다() {
        BigDecimal rate = MarketReaction.calculateChangeRate(0, 50000);

        assertThat(rate).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void 소수점_4자리까지_반올림한다() {
        BigDecimal rate = MarketReaction.calculateChangeRate(30000, 31000);

        assertThat(rate).isEqualByComparingTo(new BigDecimal("0.0333"));
    }
}
