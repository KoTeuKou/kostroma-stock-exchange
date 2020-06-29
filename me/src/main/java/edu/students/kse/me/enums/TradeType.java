package edu.students.kse.me.enums;

public enum TradeType {
    REGULAR((byte) 0), AUCTION((byte) 1);

    private final byte tradeTypeCode;

    TradeType(byte tradeTypeCode) {
        this.tradeTypeCode = tradeTypeCode;
    }

    public byte getCode() {
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
