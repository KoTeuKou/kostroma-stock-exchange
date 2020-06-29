package edu.students.kse.me;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import edu.students.kse.me.enums.ExecType;
import edu.students.kse.me.enums.OrderSide;
import edu.students.kse.me.enums.OrderTimeQualifier;
import edu.students.kse.me.enums.OrderType;
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

    private final static Duration TIMEOUT = Duration.of(10, ChronoUnit.SECONDS);
    private final static Duration TIMEOUT_FOR_DEBUG = Duration.of(10, ChronoUnit.MINUTES);

    protected ActorSystem system;

    @Before
    public void configureAkkaSystem() {
        this.system = ActorSystem.create("kse-test");
    }

    MENewOrderMessage noValidOrder = new MENewOrderMessage(
            "requestId1",
            "clientOrderId1",
            "orderId1", "clientId1",
            1L,
            OrderType.MARKET,
            OrderTimeQualifier.GOOD_TILL_CANCEL,
            OrderSide.BID,
            new BigDecimal("1"),
            new BigDecimal("5000"),
            null,
            null);

    MENewOrderMessage marketBuyOrder = new MENewOrderMessage(
            "requestId2",
            "clientOrderId2",
            "orderId2", "clientId2",
            1L,
            OrderType.MARKET,
            OrderTimeQualifier.GOOD_TILL_CANCEL,
            OrderSide.BID,
            new BigDecimal("10000"),
            new BigDecimal("10000"),
            null,
            null);

    MENewOrderMessage limitSellOrder = new MENewOrderMessage(
            "requestId3",
            "clientOrderId3",
            "orderId3", "clientId3",
            1L,
            OrderType.LIMIT,
            OrderTimeQualifier.GOOD_TILL_CANCEL,
            OrderSide.OFFER,
            new BigDecimal("4000"),
            new BigDecimal("4000"),
            new BigDecimal("300"),
            null);

    MENewOrderMessage stopLimitSellOrder = new MENewOrderMessage(
            "requestId4",
            "clientOrderId4",
            "orderId4", "clientId4",
            1L,
            OrderType.STOP_LIMIT,
            OrderTimeQualifier.GOOD_TILL_CANCEL,
            OrderSide.OFFER,
            new BigDecimal("10000"),
            new BigDecimal("10000"),
            new BigDecimal("1000"),
            new BigDecimal("600"));

    MECancelMessage cancelOrder = new MECancelMessage(
            "requestId5",
            "clientOrderId5",
            "clientOrderId3",
            "clientOrderId3",
            1L,
            OrderSide.OFFER);

    MENewOrderMessage limitSellOrder2 = new MENewOrderMessage(
            "requestId7",
            "clientOrderId7",
            "orderId7", "clientId7",
            1L,
            OrderType.LIMIT,
            OrderTimeQualifier.GOOD_TILL_CANCEL,
            OrderSide.OFFER,
            new BigDecimal("3000"),
            new BigDecimal("3000"),
            new BigDecimal("300"),
            null);

    MENewOrderMessage limitSellOrder3 = new MENewOrderMessage(
            "requestId9",
            "clientOrderId9",
            "orderId9", "clientId9",
            1L,
            OrderType.LIMIT,
            OrderTimeQualifier.GOOD_TILL_CANCEL,
            OrderSide.OFFER,
            new BigDecimal("1000"),
            new BigDecimal("1000"),
            new BigDecimal("300"),
            null);

    MENewOrderMessage limitBuyOrder = new MENewOrderMessage(
            "requestId6",
            "clientOrderId6",
            "orderId6", "clientId6",
            1L,
            OrderType.LIMIT,
            OrderTimeQualifier.FILL_OR_KILL,
            OrderSide.BID,
            new BigDecimal("7000"),
            new BigDecimal("7000"),
            new BigDecimal("400"),
            null);


    @Test
    public void testNewOrderMessage() {
        ActorRef meRef = system.actorOf(Props.create(MatchingEngine.class));
        meRef.tell(new METimeMessage(Instant.ofEpochMilli(100)), ActorRef.noSender());

        TestKit probe = new TestKit(system);

        meRef.tell(new MESubscribeMessage(), probe.getRef());

        meRef.tell(noValidOrder, probe.getRef());
        // Tests
        Object msg = probe.receiveOne(TIMEOUT);
        Assert.assertEquals(MEExecutionReport.class, msg.getClass());
        MEExecutionReport execReport = (MEExecutionReport) msg;
        TestUtils.assertEquals(noValidOrder.getLimitPrice(), execReport.getPrice());
        TestUtils.assertEquals(noValidOrder.getOrderQty(), execReport.getQty());
        msg = probe.receiveOne(TIMEOUT);
        Assert.assertEquals(TransactionComplete.class, msg.getClass());

    }

    @Test
    public void testAllTypesOfOrderMessage() {
        ActorRef meRef = system.actorOf(Props.create(MatchingEngine.class));
        meRef.tell(new METimeMessage(Instant.ofEpochMilli(100)), ActorRef.noSender());
        TestKit probe = new TestKit(system);
        meRef.tell(new MESubscribeMessage(), probe.getRef());

        meRef.tell(marketBuyOrder, probe.getRef());
        meRef.tell(limitSellOrder, probe.getRef());
        meRef.tell(cancelOrder, probe.getRef());
        meRef.tell(limitSellOrder2, probe.getRef());
        meRef.tell(limitSellOrder, probe.getRef());
        meRef.tell(marketBuyOrder, probe.getRef());
        meRef.tell(stopLimitSellOrder, probe.getRef());
        List<Object> objects = probe.receiveN(10, TIMEOUT_FOR_DEBUG);
    }

        @Test
    public void testFokOrderMessage() {

        ActorRef meRef = system.actorOf(Props.create(MatchingEngine.class));
        meRef.tell(new METimeMessage(Instant.ofEpochMilli(100)), ActorRef.noSender());
        TestKit probe = new TestKit(system);
        meRef.tell(new MESubscribeMessage(), probe.getRef());

        meRef.tell(limitSellOrder, probe.getRef());

        Object msg = probe.receiveOne(TIMEOUT);
        Assert.assertEquals(MEExecutionReport.class, msg.getClass());
        MEExecutionReport execReport = (MEExecutionReport) msg;
        Assert.assertEquals(ExecType.NEW, execReport.getExecType());
        msg = probe.receiveOne(TIMEOUT);
        Assert.assertEquals(TransactionComplete.class, msg.getClass());

        meRef.tell(limitSellOrder2, probe.getRef());

        msg = probe.receiveOne(TIMEOUT);
        Assert.assertEquals(MEExecutionReport.class, msg.getClass());
        execReport = (MEExecutionReport) msg;
        Assert.assertEquals(ExecType.NEW, execReport.getExecType());
        msg = probe.receiveOne(TIMEOUT);
        Assert.assertEquals(TransactionComplete.class, msg.getClass());

        meRef.tell(limitBuyOrder, probe.getRef());

        msg = probe.receiveOne(TIMEOUT);
        Assert.assertEquals(METradeMessage.class, msg.getClass());

        msg = probe.receiveOne(TIMEOUT);
        Assert.assertEquals(MEExecutionReport.class, msg.getClass());
        execReport = (MEExecutionReport) msg;
        Assert.assertEquals(ExecType.TRADE, execReport.getExecType());

        msg = probe.receiveOne(TIMEOUT);
        Assert.assertEquals(MEExecutionReport.class, msg.getClass());
        execReport = (MEExecutionReport) msg;
        Assert.assertEquals(ExecType.TRADE, execReport.getExecType());

        msg = probe.receiveOne(TIMEOUT);
        Assert.assertEquals(METradeMessage.class, msg.getClass());

        msg = probe.receiveOne(TIMEOUT);
        Assert.assertEquals(MEExecutionReport.class, msg.getClass());
        execReport = (MEExecutionReport) msg;
        Assert.assertEquals(ExecType.TRADE, execReport.getExecType());

        msg = probe.receiveOne(TIMEOUT);
        Assert.assertEquals(MEExecutionReport.class, msg.getClass());
        execReport = (MEExecutionReport) msg;
        Assert.assertEquals(ExecType.TRADE, execReport.getExecType());
    }
}
