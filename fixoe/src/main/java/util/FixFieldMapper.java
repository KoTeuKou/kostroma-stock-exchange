package util;

import edu.students.kse.me.enums.*;

public class FixFieldMapper {

    public static ExecType getExecTypeByValue(String value) {
        ExecType res;
        switch (value){
            case "0":
                return ExecType.NEW;
            case "4":
                return ExecType.CANCELLED;
            case "8":
                return ExecType.REJECTED;
            case "F":
                return ExecType.TRADE;
            default:
                throw new IllegalStateException("Unexpected value: " + value);
        }
    }
    public static OrderSide getOrderSideByValue(String value){
        OrderSide res;
        switch (value){
            case "1":
                return OrderSide.BID;
                
            case "2":
                return OrderSide.OFFER;
            
            default:
                throw new IllegalStateException("Unexpected value: " + value);
        }
    }

    public static OrderStatus getOrderStatusByValue(String value){
        OrderStatus res;
        switch (value){
            case "0":
                return OrderStatus.NEW;
                
            case "1":
                return OrderStatus.PARTIALLY_FILLED;
                
            case "2":
                return OrderStatus.FILLED;
                
            case "4":
                return OrderStatus.CANCELLED;
                
            case "8":
                return OrderStatus.REJECTED;
                
            default:
                throw new IllegalStateException("Unexpected value: " + value);
        }
    }

    public static OrderTimeQualifier getOrderTimeQualifierByValue(String value){
        OrderTimeQualifier res;
        switch (value){
            case "1":
                return OrderTimeQualifier.GOOD_TILL_CANCEL;
                
            case "3":
                return OrderTimeQualifier.IMMEDIATE_OR_CANCEL;
                
            case "4":
                return OrderTimeQualifier.FILL_OR_KILL;
                
            default:
                throw new IllegalStateException("Unexpected value: " + value);
        }
    }

    public static OrderType getOrderTypeByValue(String value){
        OrderType res;
        switch (value){
            case "1":
                return OrderType.MARKET;
                
            case "2":
                return OrderType.LIMIT;
                
            case "3":
                return OrderType.STOP;
                
            case "4":
                return OrderType.STOP_LIMIT;
                
            default:
                throw new IllegalStateException("Unexpected value: " + value);
        }
    }

    public static TradeType getTradeTypeByValue(String value){
        TradeType res;
        switch (value){
            case "0":
                return TradeType.REGULAR;
                
            case "1":
                return TradeType.AUCTION;
                
            default:
                throw new IllegalStateException("Unexpected value: " + value);
        }
    }
}
