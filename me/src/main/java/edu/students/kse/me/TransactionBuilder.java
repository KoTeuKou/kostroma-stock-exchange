package edu.students.kse.me;

import edu.students.kse.me.messages.MEOutputMessage;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TransactionBuilder {

    private final Instant now;

    private final List<MEOutputMessage> responses = new ArrayList<>();

    public TransactionBuilder(Instant now) {
        this.now = now;
    }

    public void add(MEOutputMessage msg) {
        responses.add(msg);
    }

    /* package */ List<MEOutputMessage> getResponses() {
        return responses;
    }
}
