package edu.students.kse.me.enums;

public enum TradeType {
    REGULAR('0'), AUCTION('1');

    private final char tradeTypeCode;

    TradeType(char tradeTypeCode) {
        this.tradeTypeCode = tradeTypeCode;
    }

    public char getCode() {
        return tradeTypeCode;
    }
}
