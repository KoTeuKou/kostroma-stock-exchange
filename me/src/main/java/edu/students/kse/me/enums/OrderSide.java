package edu.students.kse.me.enums;

public enum OrderSide {
    BID('1'), OFFER('2');

    private final char orderSideCode;

    OrderSide(char orderSideCode) {
        this.orderSideCode = orderSideCode;
    }

    public char getCode() {
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
