package com.easymoney.disclosure.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "corporate")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Corporate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String stockCode;

    @Column(nullable = false)
    private String corporateName;

    private String market;

    @Builder
    public Corporate(String stockCode, String corporateName, String market) {
        this.stockCode = stockCode;
        this.corporateName = corporateName;
        this.market = market;
    }
}
