package edu.students.kse.me;

import edu.students.kse.me.enums.OrderSide;

import java.math.BigDecimal;
import java.util.Objects;

public class OrderData {

    private final long instrumentId;

    // unique id
    private long transactionId;

    // order entry size
    private BigDecimal qty;

    // leaves qty
    private BigDecimal leavesQty;

    // order owner
    private String userId;

    // unique order identifier
    private String orderId;

    private OrderSide side;

    private BigDecimal price;

    public OrderData(long instrumentId, long transactionId, BigDecimal qty, BigDecimal leavesQty, String userId, String orderId, OrderSide side, BigDecimal price) {
        this.instrumentId = instrumentId;
        this.transactionId = transactionId;
        this.qty = qty;
        this.leavesQty = leavesQty;
        this.userId = userId;
        this.orderId = orderId;
        this.side = side;
        this.price = price;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
                Objects.equals(userId, orderData.userId) &&
                Objects.equals(qty, orderData.qty) &&
                Objects.equals(leavesQty, orderData.leavesQty) &&
                Objects.equals(orderId, orderData.orderId) &&
                side == orderData.side &&
                Objects.equals(price, orderData.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instrumentId, transactionId, qty, leavesQty, userId, orderId, side, price);
    }

    @Override
    public String toString() {
        return "OrderData{" +
                "instrumentId=" + instrumentId +
                ", transactionId=" + transactionId +
                ", qty=" + qty +
                ", leavesQty=" + leavesQty +
                ", userId=" + userId +
                ", orderId='" + orderId + '\'' +
                ", side=" + side +
                ", price=" + price +
                '}';
    }
}
