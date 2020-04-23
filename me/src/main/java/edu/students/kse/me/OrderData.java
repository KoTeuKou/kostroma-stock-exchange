package edu.students.kse.me;

import edu.students.kse.me.enums.OrderSide;

import java.math.BigDecimal;

public class OrderData {

    private final long instrumentId;

    // unique id
    private long transactionId;

    // order entry size
    private BigDecimal qty;

    // leaves qty
    private BigDecimal leavesQty;

    // order owner
    private long userId;

    // unique order identifier
    private String orderId;

    private OrderSide side;

    private BigDecimal price;

    public OrderData(long instrumentId, long transactionId, BigDecimal qty, BigDecimal leavesQty, long userId, String orderId, OrderSide side, BigDecimal price) {
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

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
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
    public String toString() {
        final StringBuilder sb = new StringBuilder("OrderData{");
        sb.append("instrumentId=").append(instrumentId);
        sb.append(", transactionId=").append(transactionId);
        sb.append(", qty=").append(qty);
        sb.append(", leavesQty=").append(leavesQty);
        sb.append(", userId=").append(userId);
        sb.append(", orderId='").append(orderId).append('\'');
        sb.append(", side=").append(side);
        sb.append(", price=").append(price);
        sb.append('}');
        return sb.toString();
    }
}
