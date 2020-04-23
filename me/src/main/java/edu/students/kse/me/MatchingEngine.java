package edu.students.kse.me;


import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import com.exactpro.kse.me.messages.*;
import edu.students.kse.me.messages.MEExecutionReport;
import edu.students.kse.me.messages.MENewOrderMessage;
import edu.students.kse.me.messages.MESubscribeMessage;
import edu.students.kse.me.messages.METimeMessage;
import edu.students.kse.me.messages.TransactionComplete;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MatchingEngine extends AbstractLoggingActor {

    private final MEIdGenerator generator;
    private final OrderBookStorage bookStorage;

    private final List<ActorRef> subscribers = new ArrayList<>();

    private Instant now;

    public MatchingEngine() {
        this.generator = new MEIdGenerator();
        this.bookStorage = new OrderBookStorage(this.generator);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(MENewOrderMessage.class, this::process)
                .match(METimeMessage.class, this::process)
                .match(MESubscribeMessage.class, this::process)
                .matchAny(message -> {
                    log().warning("{} unhandled {} from {}", getSelf(), message, getSender());
                })
                .build();
    }


    private void process(MENewOrderMessage msg) {
        TransactionBuilder collector = newTransaction();
        MEOrderBook book = bookStorage.getOrCreateOrderBook(msg.getInstrId());
        book.process(msg, collector);

        publishTransaction(collector);
    }

    private TransactionBuilder newTransaction() {
        return new TransactionBuilder(now);
    }

    private void publishTransaction(TransactionBuilder tr) {
        subscribers.forEach((ref) -> {
            // send all messages
            tr.getResponses().forEach(msg -> ref.tell(msg, getSelf()));
            // send TransactionComplete
            ref.tell(new TransactionComplete(), getSelf());
        });
    }

    private void process(METimeMessage msg) {
        this.now = msg.getTimestamp();
    }

    private void process(MESubscribeMessage msg) {
        subscribers.add(getSender());
    }

    private void publishExecutionReport(MEExecutionReport msg) {
        subscribers.forEach((ref) -> {
            ref.tell(msg, getSelf());
        });
    }
}
