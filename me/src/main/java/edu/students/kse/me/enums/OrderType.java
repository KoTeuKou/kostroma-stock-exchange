package edu.students.kse.me.enums;

public enum OrderType {
   MARKET((byte) 1), LIMIT((byte) 2), STOP((byte) 3), STOP_LIMIT((byte) 4);

   private final byte orderTypeCode;

   OrderType(byte orderTypeCode) {
      this.orderTypeCode = orderTypeCode;
   }

   public byte getCode() {
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
