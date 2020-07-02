package edu.students.kse.me.messages;

import edu.students.kse.me.enums.OrderSide;
import edu.students.kse.me.enums.OrderTimeQualifier;
import edu.students.kse.me.enums.OrderType;

import java.math.BigDecimal;
import java.util.Objects;

public class MENewOrderMessage extends MEInputMessage {

    private final String clientOrderId;

    private final String orderId;

    private final String clientId;

    private final long instrId;

    private final OrderType orderType;

    private final OrderTimeQualifier tif;

    private final OrderSide side;

    private final BigDecimal limitPrice;

    private final BigDecimal orderQty;

    private final BigDecimal displayQty;

    private final BigDecimal stopPrice;

    private final String symbol;

    public MENewOrderMessage(String clientOrderId, String orderId, String clientId, long instrId, OrderType orderType, OrderTimeQualifier tif,
                             OrderSide side, BigDecimal orderQty, BigDecimal displayQty, BigDecimal limitPrice, BigDecimal stopPrice, String symbol) {
        this.clientOrderId = clientOrderId;
        this.orderId = orderId;
        this.clientId = clientId;
        this.instrId = instrId;
        this.orderType = orderType;
        this.tif = tif;
        this.side = side;
        this.limitPrice = limitPrice;
        this.orderQty = orderQty;
        this.displayQty = displayQty;
        this.stopPrice = stopPrice;
        this.symbol = symbol;
    }

    public String getClientOrderId() {
        return clientOrderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getClientId() {
        return clientId;
    }

    public long getInstrId() {
        return instrId;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public OrderTimeQualifier getTif() {
        return tif;
    }

    public OrderSide getSide() {
        return side;
    }

    public BigDecimal getLimitPrice() {
        return limitPrice;
    }

    public BigDecimal getOrderQty() {
        return orderQty;
    }

    public BigDecimal getDisplayQty() {
        return displayQty;
    }

    public BigDecimal getStopPrice() {
        return stopPrice;
    }

    public String getSymbol() {
        return symbol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MENewOrderMessage)) return false;
        MENewOrderMessage message = (MENewOrderMessage) o;
        return instrId == message.instrId &&
                Objects.equals(clientOrderId, message.clientOrderId) &&
                Objects.equals(orderId, message.orderId) &&
                Objects.equals(clientId, message.clientId) &&
                orderType == message.orderType &&
                tif == message.tif &&
                side == message.side &&
                Objects.equals(limitPrice, message.limitPrice) &&
                Objects.equals(orderQty, message.orderQty) &&
                Objects.equals(displayQty, message.displayQty) &&
                Objects.equals(stopPrice, message.stopPrice) &&
                Objects.equals(symbol, message.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientOrderId, orderId, clientId, instrId, orderType, tif, side, limitPrice, orderQty, displayQty, stopPrice, symbol);
    }

    @Override
    public String toString() {
        return "MENewOrderMessage{" +
                ", clientOrderId='" + clientOrderId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", clientId='" + clientId + '\'' +
                ", instrId=" + instrId +
                ", orderType=" + orderType +
                ", tif=" + tif +
                ", side=" + side +
                ", limitPrice=" + limitPrice +
                ", orderQty=" + orderQty +
                ", displayQty=" + displayQty +
                ", stopPrice=" + stopPrice +
                ", symbol=" + symbol +
                '}';
    }
}
