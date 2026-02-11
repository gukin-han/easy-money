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

import java.time.LocalDateTime;

@Entity
@Table(name = "disclosure")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Disclosure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String receiptNumber;

    @Column(nullable = false)
    private String corporateName;

    private String stockCode;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDateTime disclosedAt;

    private String documentUrl;

    @Builder
    public Disclosure(String receiptNumber, String corporateName, String stockCode,
                      String title, LocalDateTime disclosedAt, String documentUrl) {
        this.receiptNumber = receiptNumber;
        this.corporateName = corporateName;
        this.stockCode = stockCode;
        this.title = title;
        this.disclosedAt = disclosedAt;
        this.documentUrl = documentUrl;
    }
}
