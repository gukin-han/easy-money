package com.easymoney.disclosure.infrastructure.classifier;

import com.easymoney.disclosure.domain.model.DisclosureCategory;
import com.easymoney.disclosure.domain.service.DisclosureClassifier;
import org.springframework.stereotype.Component;

@Component
public class TitleBasedDisclosureClassifier implements DisclosureClassifier {

    @Override
    public DisclosureCategory classify(String title) {
        if (title == null) {
            return DisclosureCategory.OTHER;
        }

        // === IGNORABLE ===

        if (title.contains("[기재정정]")) {
            return DisclosureCategory.CORRECTION;
        }
        if (title.contains("첨부추가")) {
            return DisclosureCategory.ATTACHMENT;
        }
        if (title.contains("증권신고서") && title.contains("정정")) {
            return DisclosureCategory.AMENDMENT;
        }
        if (title.contains("장외회사")) {
            return DisclosureCategory.SHELL_COMPANY;
        }
        if (title.contains("주주총회소집") || title.contains("주주총회집중일")) {
            return DisclosureCategory.SHAREHOLDER_MEETING;
        }
        if (title.contains("주식매수선택권부여에관한신고")) {
            return DisclosureCategory.STOCK_OPTION;
        }
        if (title.contains("의결권대리행사권유")) {
            return DisclosureCategory.PROXY;
        }
        if (title.contains("증권발행실적보고서")) {
            return DisclosureCategory.SECURITIES_REPORT;
        }
        if (title.contains("지급수단별") && title.contains("지급기간별")) {
            return DisclosureCategory.PAYMENT_SCHEDULE;
        }
        if (title.contains("주권매매거래정지")) {
            return DisclosureCategory.TRADING_HALT;
        }
        if (title.contains("불성실공시법인지정")) {
            return DisclosureCategory.UNFAITHFUL_DISCLOSURE;
        }

        // === ANALYZABLE ===

        if (title.contains("사업보고서") || title.contains("분기보고서")
                || title.contains("반기보고서")) {
            return DisclosureCategory.REGULAR_REPORT;
        }
        if (title.contains("주요사항보고")) {
            return DisclosureCategory.MATERIAL_EVENT;
        }
        if (title.contains("지분변동") || title.contains("임원ㆍ주요주주")
                || title.contains("주식등의대량보유")) {
            return DisclosureCategory.OWNERSHIP_CHANGE;
        }
        if (title.contains("공개매수")) {
            return DisclosureCategory.TENDER_OFFER;
        }
        if (title.contains("매출액또는손익구조") || title.contains("파생상품거래손실")) {
            return DisclosureCategory.FINANCIAL_CHANGE;
        }
        if (title.contains("현금ㆍ현물배당결정") || title.contains("배당")) {
            return DisclosureCategory.DIVIDEND;
        }
        if (title.contains("영업(잠정)실적") || title.contains("잠정실적")
                || title.contains("연결재무제표기준영업")) {
            return DisclosureCategory.EARNINGS;
        }
        if (title.contains("감사보고서")) {
            return DisclosureCategory.AUDIT_REPORT;
        }
        if (title.contains("단일판매ㆍ공급계약") || title.contains("계약체결")
                || title.contains("계약해지")) {
            return DisclosureCategory.CONTRACT;
        }
        if (title.contains("소송등의제기") || title.contains("소송")) {
            return DisclosureCategory.LITIGATION;
        }
        if (title.contains("타인에대한채무보증") || title.contains("담보제공")
                || title.contains("채무보증")) {
            return DisclosureCategory.GUARANTEE;
        }
        if (title.contains("특수관계인") || title.contains("동일인등출자계열회사")) {
            return DisclosureCategory.RELATED_PARTY;
        }
        if (title.contains("관리종목지정") || title.contains("상장폐지")
                || title.contains("상장적격성")) {
            return DisclosureCategory.MANAGEMENT_ISSUE;
        }
        if (title.contains("유상증자") || title.contains("무상증자")
                || title.contains("자본감소") || title.contains("자기주식")) {
            return DisclosureCategory.CAPITAL_CHANGE;
        }

        return DisclosureCategory.OTHER;
    }
}
