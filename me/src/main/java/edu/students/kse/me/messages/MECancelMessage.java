package edu.students.kse.me.messages;

import java.util.Objects;

public class MECancelMessage extends MEInputMessage  {

    private final String clientOrderId;

    private final String clientId;

    private final String originalClientOrderId;

    private final String orderId;

    private final long instrId;

    private final byte side;

    public MECancelMessage(String clientOrderId, String clientId, String originalClientOrderId, String orderId, long instrId, byte side) {
        this.clientOrderId = clientOrderId;
        this.clientId = clientId;
        this.originalClientOrderId = originalClientOrderId;
        this.orderId = orderId;
        this.instrId = instrId;
        this.side = side;
    }

    public String getClientOrderId() {
        return clientOrderId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getOriginalClientOrderId() {
        return originalClientOrderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public long getInstrId() {
        return instrId;
    }

    public byte getSide() {
        return side;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MECancelMessage)) return false;
        MECancelMessage that = (MECancelMessage) o;
        return instrId == that.instrId &&
                side == that.side &&
                Objects.equals(clientOrderId, that.clientOrderId) &&
                Objects.equals(clientId, that.clientId) &&
                Objects.equals(originalClientOrderId, that.originalClientOrderId) &&
                Objects.equals(orderId, that.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientOrderId, clientId, originalClientOrderId, orderId, instrId, side);
    }

    @Override
    public String toString() {
        return "MECancelMessage{" +
                "clientOrderId='" + clientOrderId + '\'' +
                ", clientId='" + clientId + '\'' +
                ", originalClientOrderId='" + originalClientOrderId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", instrId=" + instrId +
                ", side=" + side +
                '}';
    }
}
