package edu.students.kse.me.messages;

import edu.students.kse.me.enums.ExecType;
import edu.students.kse.me.enums.OrderStatus;

import java.math.BigDecimal;
import java.util.Objects;

public class MEExecutionReport extends MEOutputMessage {

    private final String execId;

    private final String clientId;

    private final String clientOrderId;

    private final String orderId;

    private final ExecType execType;

    private final OrderStatus orderStatus;

    private final BigDecimal price;

    private final BigDecimal qty;

    private final BigDecimal executedPrice;

    private final BigDecimal executedQty;

    private final String tradeMatchId;

    public MEExecutionReport(String execId, String clientId, String clientOrderId, String orderId, ExecType execType,
     OrderStatus orderStatus, BigDecimal price, BigDecimal qty, BigDecimal executedPrice, BigDecimal executedQty, String tradeMatchId) {
        this.execId = execId;
        this.clientId = clientId;
        this.clientOrderId = clientOrderId;
        this.orderId = orderId;
        this.execType = execType;
        this.orderStatus = orderStatus;
        this.price = price;
        this.qty = qty;
        this.executedPrice = executedPrice;
        this.executedQty = executedQty;
        this.tradeMatchId = tradeMatchId;
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

    public ExecType getExecType() {
        return execType;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public BigDecimal getExecutedPrice() {
        return executedPrice;
    }

    public BigDecimal getExecutedQty() {
        return executedQty;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getQty() {
        return qty;
    }

    public String getTradeMatchId() {
        return tradeMatchId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MEExecutionReport)) return false;
        MEExecutionReport that = (MEExecutionReport) o;
        return Objects.equals(execId, that.execId) &&
                Objects.equals(clientId, that.clientId) &&
                Objects.equals(clientOrderId, that.clientOrderId) &&
                Objects.equals(orderId, that.orderId) &&
                execType == that.execType &&
                orderStatus == that.orderStatus &&
                Objects.equals(price, that.price) &&
                Objects.equals(qty, that.qty) &&
                Objects.equals(executedPrice, that.executedPrice) &&
                Objects.equals(executedQty, that.executedQty) &&
                Objects.equals(tradeMatchId, that.tradeMatchId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(execId, clientId, clientOrderId, orderId, execType, orderStatus, price, qty, executedPrice, executedQty, tradeMatchId);
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
                ", qty=" + qty +
                ", executedPrice=" + executedPrice +
                ", executedQty=" + executedQty +
                ", tradeMatchId='" + tradeMatchId + '\'' +
                '}';
    }
}
