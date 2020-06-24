package edu.students.kse.me;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import edu.students.kse.me.messages.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class METest {

    private final static Duration TIMEOUT = Duration.of(10000, ChronoUnit.SECONDS);

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

        MENewOrderMessage noValidOrder = new MENewOrderMessage(
                "requestId1",
                "clientOrderId1",
                "clientId1",
                1L,
                (byte) 1,
                (byte) 1,
                (byte) 1,
                new BigDecimal("1"),
                new BigDecimal("5000"),
                new BigDecimal("1000"),
                null);

        MENewOrderMessage marketBuyOrder = new MENewOrderMessage(
                "requestId2",
                "clientOrderId2",
                "clientId2",
                1L,
                (byte) 1,
                (byte) 1,
                (byte) 1,
                new BigDecimal("10000"),
                new BigDecimal("5000"),
                new BigDecimal("1000"),
                null);

        MENewOrderMessage limitSellOrder = new MENewOrderMessage(
                "requestId3",
                "clientOrderId3",
                "clientId3",
                1L,
                (byte) 2,
                (byte) 1,
                (byte) 2,
                new BigDecimal("10000"),
                new BigDecimal("5000"),
                new BigDecimal("500"),
                null);

        MENewOrderMessage stopLimitSellOrder = new MENewOrderMessage(
                "requestId4",
                "clientOrderId4",
                "clientId4",
                1L,
                (byte) 4,
                (byte) 1,
                (byte) 2,
                new BigDecimal("10000"),
                new BigDecimal("5000"),
                new BigDecimal("1000"),
                new BigDecimal("500"));

        MECancelMessage cancelOrder = new MECancelMessage(
                "requestId3",
                "clientOrderId3",
                "clientOrderId3",
                "clientOrderId3",
                1L,
                (byte) 2);

        meRef.tell(noValidOrder, probe.getRef());

        // Tests
        Object msg = probe.receiveOne(TIMEOUT);
        Assert.assertEquals(MEExecutionReport.class, msg.getClass());
        MEExecutionReport execReport = (MEExecutionReport) msg;
        TestUtils.assertEquals(noValidOrder.getLimitPrice(), execReport.getPrice());
        TestUtils.assertEquals(noValidOrder.getOrderQty(), execReport.getLeavesQty());
        msg = probe.receiveOne(TIMEOUT);
        Assert.assertEquals(TransactionComplete.class, msg.getClass());


        // just for check ME
        meRef.tell(marketBuyOrder, probe.getRef());
        meRef.tell(limitSellOrder, probe.getRef());
        meRef.tell(cancelOrder, probe.getRef());
        meRef.tell(limitSellOrder, probe.getRef());
        meRef.tell(marketBuyOrder, probe.getRef());
        meRef.tell(stopLimitSellOrder, probe.getRef());
        List<Object> objects = probe.receiveN(10, TIMEOUT);

    }
}
