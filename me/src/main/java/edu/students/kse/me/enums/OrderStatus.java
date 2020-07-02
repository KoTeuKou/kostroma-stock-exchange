package edu.students.kse.me.enums;

public enum OrderStatus {
    NEW('0'), PARTIALLY_FILLED('1'), FILLED('2'), CANCELLED('4') , REJECTED('8');

    private final char orderStatusCode;

    OrderStatus(char orderStatusCode) {
        this.orderStatusCode = orderStatusCode;
    }

    public char getCode() {
        return orderStatusCode;
    }
}
