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
}
