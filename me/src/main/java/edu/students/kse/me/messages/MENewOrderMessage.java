package edu.students.kse.me.messages;

import java.math.BigDecimal;
import java.util.Objects;

public class MENewOrderMessage extends MEInputMessage {

    private final String requestId;

    private final String clientOrderId;

    private final String clientId;

    private final long instrId;

    private final byte orderType;

    private final byte tif;

    private final byte side;

    private final BigDecimal limitPrice;

    private final BigDecimal orderQty;

    private final BigDecimal displayQty;

    private final BigDecimal stopPrice;


    public MENewOrderMessage(String requestId, String clientOrderId, String clientId, long instrId, byte orderType, byte tif,
                             byte side, BigDecimal orderQty, BigDecimal displayQty, BigDecimal limitPrice, BigDecimal stopPrice) {
        this.requestId = requestId;
        this.clientOrderId = clientOrderId;
        this.clientId = clientId;
        this.instrId = instrId;
        this.orderType = orderType;
        this.tif = tif;
        this.side = side;
        this.limitPrice = limitPrice;
        this.orderQty = orderQty;
        this.displayQty = displayQty;
        this.stopPrice = stopPrice;
    }

    public String getClientOrderId() {
        return clientOrderId;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getClientId() {
        return clientId;
    }

    public long getInstrId() {
        return instrId;
    }

    public byte getOrderType() {
        return orderType;
    }

    public byte getTif() {
        return tif;
    }

    public byte getSide() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MENewOrderMessage)) return false;
        MENewOrderMessage that = (MENewOrderMessage) o;
        return instrId == that.instrId &&
                orderType == that.orderType &&
                tif == that.tif &&
                side == that.side &&
                Objects.equals(requestId, that.requestId) &&
                Objects.equals(clientOrderId, that.clientOrderId) &&
                Objects.equals(clientId, that.clientId) &&
                Objects.equals(limitPrice, that.limitPrice) &&
                Objects.equals(orderQty, that.orderQty) &&
                Objects.equals(displayQty, that.displayQty) &&
                Objects.equals(stopPrice, that.stopPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId, clientOrderId, clientId, instrId, orderType, tif, side, limitPrice, orderQty, displayQty, stopPrice);
    }

    @Override
    public String toString() {
        return "MENewOrderMessage{" +
                "requestId='" + requestId + '\'' +
                ", clientOrderId='" + clientOrderId + '\'' +
                ", clientId='" + clientId + '\'' +
                ", instrId=" + instrId +
                ", orderType=" + orderType +
                ", tif=" + tif +
                ", side=" + side +
                ", limitPrice=" + limitPrice +
                ", orderQty=" + orderQty +
                ", displayQty=" + displayQty +
                ", stopPrice=" + stopPrice +
                '}';
    }
}
