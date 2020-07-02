package edu.students.kse.me.enums;

public enum OrderType {
   MARKET('1'), LIMIT('2'), STOP('3'), STOP_LIMIT('4');

   private final char orderTypeCode;

   OrderType(char orderTypeCode) {
      this.orderTypeCode = orderTypeCode;
   }

   public char getCode() {
      return orderTypeCode;
   }
}
