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

    public static OrderStatus getEnumByValue(String value){
        OrderStatus res;
        switch (value){
            case "0":
                res = NEW;
                break;
            case "1":
                res = PARTIALLY_FILLED;
                break;
            case "2":
                res = FILLED;
                break;
            case "4":
                res = CANCELLED;
                break;
            case "8":
                res = REJECTED;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + value);
        }
        return res;
    }
}
