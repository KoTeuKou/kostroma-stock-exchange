package edu.students.kse.me.enums;

public enum ExecType {
    NEW('0'), CANCELLED('4') , REJECTED('8'), TRADE('F');

    private final char execTypeCode;

    ExecType(char execTypeCode) {
        this.execTypeCode = execTypeCode;
    }

    public char getCode() {
        return execTypeCode;
    }

    public static ExecType getEnumByValue(String value){
        ExecType res;
        switch (value){
            case "0":
                res = NEW;
                break;
            case "4":
                res = CANCELLED;
                break;
            case "8":
                res = REJECTED;
                break;
            case "F":
                res = TRADE;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + value);
        }
        return res;
    }
}
