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
}
