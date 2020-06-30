package edu.students.kse.me;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import edu.students.kse.me.messages.METimeMessage;

import java.time.Instant;

public class METimer extends AbstractActor {

    private final ActorRef meRef;

    @Override
    public void preStart() throws Exception {
        super.preStart();
        getContext().getSystem().scheduler().schedule(java.time.Duration.ofMillis(0), java.time.Duration.ofMillis(250),
                meRef, new METimeMessage(Instant.now()), getContext().getSystem().dispatcher(), ActorRef.noSender());
    }

    public METimer(ActorRef meRef) {
        this.meRef = meRef;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(METimeMessage.class, this::process)
                .build();
    }

    private void process(METimeMessage timeMessage) {
        meRef.tell(timeMessage, getSender());
    }
}
