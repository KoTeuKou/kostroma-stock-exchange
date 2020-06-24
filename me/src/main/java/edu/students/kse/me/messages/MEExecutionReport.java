package edu.students.kse.me.messages;

import java.math.BigDecimal;
import java.util.Objects;

public class MEExecutionReport extends MEOutputMessage {

    private final String execId;

    private final String clientId;

    private final String clientOrderId;

    private final String orderId;

    private final char execType;

    private final char orderStatus;

    private final BigDecimal price;

    private final BigDecimal leavesQty;

    public MEExecutionReport(String execId, String clientId, String clientOrderId, String orderId, char execType, char orderStatus, BigDecimal price, BigDecimal leavesQty) {
        this.execId = execId;
        this.clientId = clientId;
        this.clientOrderId = clientOrderId;
        this.orderId = orderId;
        this.execType = execType;
        this.orderStatus = orderStatus;
        this.price = price;
        this.leavesQty = leavesQty;
    }

    public String getExecId() {
        return execId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientOrderId() {
        return clientOrderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public char getExecType() {
        return execType;
    }

    public char getOrderStatus() {
        return orderStatus;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getLeavesQty() {
        return leavesQty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MEExecutionReport)) return false;
        MEExecutionReport that = (MEExecutionReport) o;
        return execType == that.execType &&
                orderStatus == that.orderStatus &&
                Objects.equals(execId, that.execId) &&
                Objects.equals(clientId, that.clientId) &&
                Objects.equals(clientOrderId, that.clientOrderId) &&
                Objects.equals(orderId, that.orderId) &&
                Objects.equals(price, that.price) &&
                Objects.equals(leavesQty, that.leavesQty);
    }

    @Override
    public int hashCode() {
        return Objects.hash(execId, clientId, clientOrderId, orderId, execType, orderStatus, price, leavesQty);
    }

    @Override
    public String toString() {
        return "MEExecutionReport{" +
                "execId='" + execId + '\'' +
                ", clientId='" + clientId + '\'' +
                ", clientOrderId='" + clientOrderId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", execType=" + execType +
                ", orderStatus=" + orderStatus +
                ", price=" + price +
                ", leavesQty=" + leavesQty +
                '}';
    }
}
