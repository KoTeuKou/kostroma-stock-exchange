package edu.students.kse.me;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import edu.students.kse.me.messages.METimeMessage;

import java.time.Duration;
import java.time.Instant;

public class METimer extends AbstractActor {

    private final ActorRef meRef;
    private Cancellable schedule;

    @Override
    public void preStart() throws Exception {
        super.preStart();
        ActorSystem system = getContext().getSystem();
        schedule = system.scheduler().schedule(Duration.ofMillis(0), Duration.ofMillis(250),
                meRef, new METimeMessage(Instant.now()), getContext().getSystem().dispatcher(), ActorRef.noSender());
    }

    @Override
    public void postStop() throws Exception {
        schedule.cancel();
        super.postStop();
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
