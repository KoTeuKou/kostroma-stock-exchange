package edu.students.kse.me.enums;

public enum OrderSide {
    BID((byte) 1), OFFER((byte)2);

    private final byte orderSideCode;

    OrderSide(byte orderSideCode) {
        this.orderSideCode = orderSideCode;
    }

    public byte getCode() {
        return orderSideCode;
    }

    public static OrderSide getEnumByValue(String value){
        OrderSide res;
        switch (value){
            case "1":
                res = BID;
                break;
            case "2":
                res = OFFER;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + value);
        }
        return res;
    }
}
