package edu.students.kse.me;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import edu.students.kse.me.messages.METimeMessage;

import java.time.Instant;

public class METimer extends AbstractActor {

    private final ActorRef meRef;

    public METimer(ActorRef meRef) {
        this.meRef = meRef;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("getTime", this::process)
                .build();
    }

    private void process(String ignored) {
        meRef.tell(new METimeMessage(Instant.now()), getSender());
    }
}
