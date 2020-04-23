package edu.students.kse.me;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import edu.students.kse.me.messages.MEExecutionReport;
import edu.students.kse.me.messages.MENewOrderMessage;
import edu.students.kse.me.messages.MESubscribeMessage;
import edu.students.kse.me.messages.METimeMessage;
import edu.students.kse.me.messages.TransactionComplete;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class METest {

    private final static Duration TIMEOUT = Duration.of(1, ChronoUnit.SECONDS);

    protected ActorSystem system;

    @Before
    public void configureAkkaSystem() {
        this.system = ActorSystem.create("kse-test");
    }

    @Test
    public void testNewOrderMessage() {
        ActorRef meRef = system.actorOf(Props.create(MatchingEngine.class));
        meRef.tell(new METimeMessage(Instant.ofEpochMilli(100)), ActorRef.noSender());

        TestKit probe = new TestKit(system);

        meRef.tell(new MESubscribeMessage(), probe.getRef());

        MENewOrderMessage newOrderMessage = new MENewOrderMessage(
                "requestId",
                1L,
                new BigDecimal("10"),
                new BigDecimal("20")
        );
        meRef.tell(newOrderMessage, probe.getRef());

        Object msg = probe.receiveOne(TIMEOUT);
        Assert.assertEquals(MEExecutionReport.class, msg.getClass());
        MEExecutionReport execReport = (MEExecutionReport) msg;
        // FIXME: fix tests
//        TestUtils.assertEquals(newOrderMessage.getPrice(), execReport.getPrice());
//        TestUtils.assertEquals(newOrderMessage.getSize(), execReport.getLeavesQty());


        msg = probe.receiveOne(TIMEOUT);
        Assert.assertEquals(TransactionComplete.class, msg.getClass());
    }
}
