package edu.students.kse.fixoe;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import edu.students.kse.me.messages.MENewOrderMessage;
import edu.students.kse.me.messages.MEOutputMessage;
import edu.students.kse.me.messages.MESubscribeMessage;

import java.io.FileNotFoundException;
import java.net.URL;

public class FixServerActor extends AbstractLoggingActor {

    private final ActorRef meRef;
    private FixApplication app;

    public FixServerActor(ActorRef meRef) {
        this.meRef = meRef;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(MEOutputMessage.class, this::process)
                .match(MENewOrderMessage.class, this::process)
                .matchAny(message -> {
                    log().warning("{} unhandled {} from {}", getSelf(), message, getSender());
                })
                .build();
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();

        URL resource = FixApplication.class.getClassLoader().getResource("config.cfg");
        if (resource == null) {
            String msg = "Config cannot be loaded";
            log().error(msg);
            throw new FileNotFoundException(msg);
        }
        app = new FixApplication(getSelf(), resource.openStream());
        app.start();

        // subscribe to ME
        meRef.tell(new MESubscribeMessage(), getSelf());
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();

        app.stop();
    }


    private void process(MEOutputMessage msg) {
        app.sendMessage(msg);
    }

    private void process(MENewOrderMessage msg) {
        meRef.tell(msg, getSender());
    }
}
