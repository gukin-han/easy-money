package com.easymoney.disclosure.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DisclosureCategory {

    // IGNORABLE — LLM 분석 불필요
    CORRECTION("기재정정", false),
    ATTACHMENT("첨부추가", false),
    AMENDMENT("증권신고서정정", false),
    SHELL_COMPANY("장외회사", false),
    SHAREHOLDER_MEETING("주주총회", false),
    STOCK_OPTION("주식매수선택권", false),
    PROXY("의결권대리행사", false),
    SECURITIES_REPORT("증권발행실적보고서", false),
    PAYMENT_SCHEDULE("지급수단별지급기간별", false),
    TRADING_HALT("주권매매거래정지", false),
    UNFAITHFUL_DISCLOSURE("불성실공시법인지정", false),

    // ANALYZABLE — 투자 판단 영향
    REGULAR_REPORT("정기보고서", true),
    MATERIAL_EVENT("주요사항", true),
    OWNERSHIP_CHANGE("지분변동", true),
    TENDER_OFFER("공개매수", true),
    FINANCIAL_CHANGE("재무변동", true),
    DIVIDEND("배당결정", true),
    EARNINGS("잠정실적", true),
    AUDIT_REPORT("감사보고서", true),
    CONTRACT("주요계약", true),
    LITIGATION("소송", true),
    GUARANTEE("채무보증/담보", true),
    RELATED_PARTY("특수관계인거래", true),
    MANAGEMENT_ISSUE("경영위기", true),
    CAPITAL_CHANGE("자본변동", true),

    OTHER("기타", true);

    private final String description;
    private final boolean analyzable;
}
