package edu.students.kse.me.enums;

public enum OrderTimeQualifier {
    GOOD_TILL_CANCEL('1'), IMMEDIATE_OR_CANCEL('3'), FILL_OR_KILL('4');

    private final char tifCode;

    OrderTimeQualifier(char tifCode) {
        this.tifCode = tifCode;
    }

    public char getCode() {
        return tifCode;
    }

    public static OrderTimeQualifier getEnumByValue(String value){
        OrderTimeQualifier res;
        switch (value){
            case "1":
                res = GOOD_TILL_CANCEL;
                break;
            case "3":
                res = IMMEDIATE_OR_CANCEL;
                break;
            case "4":
                res = FILL_OR_KILL;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + value);
        }
        return res;
    }
}
