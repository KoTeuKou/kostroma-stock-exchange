package edu.students.kse.me;

import edu.students.kse.me.enums.OrderSide;

import java.math.BigDecimal;
import java.util.Objects;

public class OrderData {

    private final long instrumentId;

    // unique id
    private long transactionId;

    // order owner
    private String clientId;

    private String clientOrderId;

    // unique order identifier
    private String orderId;

    private OrderSide side;

    private BigDecimal price;

    // order entry size
    private BigDecimal qty;

    // leaves qty
    private BigDecimal leavesQty;

    public OrderData(long instrumentId, long transactionId, String clientId, String clientOrderId, String orderId, OrderSide side, BigDecimal price, BigDecimal qty, BigDecimal leavesQty) {
        this.instrumentId = instrumentId;
        this.transactionId = transactionId;
        this.clientId = clientId;
        this.clientOrderId = clientOrderId;
        this.orderId = orderId;
        this.side = side;
        this.price = price;
        this.qty = qty;
        this.leavesQty = leavesQty;
    }


    public String getClientOrderId() {
        return clientOrderId;
    }

    public void setClientOrderId(String clientOrderId) {
        this.clientOrderId = clientOrderId;
    }

    public long getInstrumentId() {
        return instrumentId;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    public BigDecimal getQty() {
        return qty;
    }

    public void setQty(BigDecimal qty) {
        this.qty = qty;
    }

    public BigDecimal getLeavesQty() {
        return leavesQty;
    }

    public void setLeavesQty(BigDecimal leavesQty) {
        this.leavesQty = leavesQty;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public OrderSide getSide() {
        return side;
    }

    public void setSide(OrderSide side) {
        this.side = side;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderData)) return false;
        OrderData orderData = (OrderData) o;
        return instrumentId == orderData.instrumentId &&
                transactionId == orderData.transactionId &&
                Objects.equals(clientId, orderData.clientId) &&
                Objects.equals(clientOrderId, orderData.clientOrderId) &&
                Objects.equals(orderId, orderData.orderId) &&
                side == orderData.side &&
                Objects.equals(price, orderData.price) &&
                Objects.equals(qty, orderData.qty) &&
                Objects.equals(leavesQty, orderData.leavesQty);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instrumentId, transactionId, clientId, clientOrderId, orderId, side, price, qty, leavesQty);
    }

    @Override
    public String toString() {
        return "OrderData{" +
                "instrumentId=" + instrumentId +
                ", transactionId=" + transactionId +
                ", clientId='" + clientId + '\'' +
                ", clientOrderId='" + clientOrderId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", side=" + side +
                ", price=" + price +
                ", qty=" + qty +
                ", leavesQty=" + leavesQty +
                '}';
    }

}
