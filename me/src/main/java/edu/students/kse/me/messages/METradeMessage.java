package edu.students.kse.me.messages;

import edu.students.kse.me.enums.TradeType;

import java.math.BigDecimal;
import java.util.Objects;

public class METradeMessage extends MEOutputMessage {

    private final String tradeId;

    private final String buyerId;

    private final String sellerId;

    private final String buyerOrderId;

    private final String sellerOrderId;

    private final BigDecimal executedPrice;

    private final BigDecimal executedQty;

    private final long instrumentId;

    private final TradeType tradeType;

    public METradeMessage(String tradeId, String buyerId, String sellerId, String buyerOrderId, String sellerOrderId,
                          BigDecimal executedPrice, BigDecimal executedQty, long instrumentId, TradeType tradeType) {
        this.tradeId = tradeId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.buyerOrderId = buyerOrderId;
        this.sellerOrderId = sellerOrderId;
        this.executedPrice = executedPrice;
        this.executedQty = executedQty;
        this.instrumentId = instrumentId;
        this.tradeType = tradeType;
    }

    public String getTradeId() {
        return tradeId;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public String getBuyerOrderId() {
        return buyerOrderId;
    }

    public String getSellerOrderId() {
        return sellerOrderId;
    }

    public BigDecimal getExecutedPrice() {
        return executedPrice;
    }

    public BigDecimal getExecutedQty() {
        return executedQty;
    }

    public long getInstrumentId() {
        return instrumentId;
    }

    public TradeType getTradeType() {
        return tradeType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof METradeMessage)) return false;
        METradeMessage that = (METradeMessage) o;
        return instrumentId == that.instrumentId &&
                Objects.equals(tradeId, that.tradeId) &&
                Objects.equals(buyerId, that.buyerId) &&
                Objects.equals(sellerId, that.sellerId) &&
                Objects.equals(buyerOrderId, that.buyerOrderId) &&
                Objects.equals(sellerOrderId, that.sellerOrderId) &&
                Objects.equals(executedPrice, that.executedPrice) &&
                Objects.equals(executedQty, that.executedQty) &&
                tradeType == that.tradeType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tradeId, buyerId, sellerId, buyerOrderId, sellerOrderId, executedPrice, executedQty, instrumentId, tradeType);
    }

    @Override
    public String toString() {
        return "METradeMessage{" +
                "tradeId='" + tradeId + '\'' +
                ", buyerId='" + buyerId + '\'' +
                ", sellerId='" + sellerId + '\'' +
                ", buyerOrderId='" + buyerOrderId + '\'' +
                ", sellerOrderId='" + sellerOrderId + '\'' +
                ", executedPrice=" + executedPrice +
                ", executedQty=" + executedQty +
                ", instrumentId=" + instrumentId +
                ", tradeType=" + tradeType +
                '}';
    }
}
