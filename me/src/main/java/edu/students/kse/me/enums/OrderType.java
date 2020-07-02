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

   public static OrderType getEnumByValue(String value){
      OrderType res;
      switch (value){
         case "1":
            res = MARKET;
            break;
         case "2":
            res = LIMIT;
            break;
         case "3":
            res = STOP;
            break;
         case "4":
            res = STOP_LIMIT;
            break;
         default:
            throw new IllegalStateException("Unexpected value: " + value);
      }
      return res;
   }
}
