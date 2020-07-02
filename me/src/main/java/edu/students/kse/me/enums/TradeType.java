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

    public static TradeType getEnumByValue(String value){
        TradeType res;
        switch (value){
            case "0":
                res = REGULAR;
                break;
            case "1":
                res = AUCTION;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + value);
        }
        return res;
    }
}
