package edu.students.kse.me.messages;

import java.math.BigDecimal;

public class MENewOrderMessage extends MEInputMessage {

    private final String requestId;

    // unique instrument identifier
    private final long instrId;

    private final BigDecimal price;

    private final BigDecimal size;

    public MENewOrderMessage(String requestId, long instrId, BigDecimal price, BigDecimal size) {
        this.requestId = requestId;
        this.instrId = instrId;
        this.price = price;
        this.size = size;
    }

    public String getRequestId() {
        return requestId;
    }

    public long getInstrId() {
        return instrId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getSize() {
        return size;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MENewOrderMessage{");
        sb.append("requestId='").append(requestId).append('\'');
        sb.append(", instrId=").append(instrId);
        sb.append(", price=").append(price);
        sb.append(", size=").append(size);
        sb.append('}');
        return sb.toString();
    }
}
