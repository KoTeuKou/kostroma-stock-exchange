package edu.students.kse.me;
import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import edu.students.kse.me.messages.*;

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
                .match(MECancelMessage.class, this::process)
                .match(MENewOrderMessage.class, this::process)
                .match(METimeMessage.class, this::process)
                .match(MESubscribeMessage.class, this::process)
                .matchAny(message -> {
                    log().warning("{} unhandled {} from {}", getSelf(), message, getSender());
                })
                .build();
    }


    private void process(MEInputMessage msg) {
        TransactionBuilder collector = newTransaction();
        if (msg instanceof MENewOrderMessage) {
            MENewOrderMessage orderMessage = (MENewOrderMessage) msg;
            MEOrderBook book = bookStorage.getOrCreateOrderBook(orderMessage.getInstrId());
            book.process(orderMessage, collector);
        }
        else {
            MECancelMessage cancelMessage = (MECancelMessage) msg;
            MEOrderBook book = bookStorage.getOrCreateOrderBook(cancelMessage.getInstrId());
            book.process(cancelMessage, collector);
        }

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
