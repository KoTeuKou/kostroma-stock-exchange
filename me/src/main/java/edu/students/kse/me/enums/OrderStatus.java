package edu.students.kse.me.enums;

public enum OrderStatus {
    NEW((byte) 0), PARTIALLY_FILLED((byte) 1), FILLED((byte) 2), CANCELLED((byte) 4) , REJECTED((byte) 8);

    private final byte orderStatusCode;

    OrderStatus(byte orderStatusCode) {
        this.orderStatusCode = orderStatusCode;
    }

    public byte getCode() {
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
