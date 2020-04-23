package edu.students.kse.me.messages;

import java.time.Instant;

public class METimeMessage extends MEOutputMessage {

    private final Instant timestamp;

    public METimeMessage(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TimeMessage{");
        sb.append("timestamp=").append(timestamp);
        sb.append('}');
        return sb.toString();
    }
}
