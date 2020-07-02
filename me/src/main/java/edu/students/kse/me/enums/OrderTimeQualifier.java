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
}
