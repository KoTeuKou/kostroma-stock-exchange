package edu.students.kse.me;

import edu.students.kse.me.enums.*;
import edu.students.kse.me.messages.MECancelMessage;
import edu.students.kse.me.messages.MEExecutionReport;
import edu.students.kse.me.messages.MENewOrderMessage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import edu.students.kse.me.messages.METradeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MEOrderBook {

    private final long instrumentId;
    private static final BigDecimal minSize = new BigDecimal("1000");
    private final ArrayList<OrderData> bids = new ArrayList<>();
    private final ArrayList<OrderData> offers = new ArrayList<>();
    private final ArrayList<MENewOrderMessage> stopOffers = new ArrayList<>();
    private final ArrayList<MENewOrderMessage> stopBids = new ArrayList<>();
    private final MEIdGenerator generator;
    private final Logger logger = LoggerFactory.getLogger(MEOrderBook.class);


    public MEOrderBook(long instrumentId, MEIdGenerator generator) {
        this.instrumentId = instrumentId;
        this.generator = generator;
    }

    public void process(MECancelMessage meCancelMessage, TransactionBuilder collector){
        OrderData reqOrderData;
        ArrayList<OrderData> orders;
        if (meCancelMessage.getSide() == OrderSide.BID){
            orders = bids;
        }
        else {
            orders = offers;
        }
        reqOrderData = orders.stream()
                .filter(orderData -> orderData.getClientOrderId().equals(meCancelMessage.getOriginalClientOrderId()))
                .findFirst().orElse(null);
        // If such order has been found => remove from book & create report
        if (reqOrderData != null) {
            if (reqOrderData.getClientId().equals(meCancelMessage.getClientId())) {
                orders.remove(reqOrderData);
                collector.add(new MEExecutionReport(generator.getNextExecutionId(), meCancelMessage.getClientId(),
                        meCancelMessage.getClientOrderId(), reqOrderData.getOrderId(), ExecType.CANCELLED, OrderStatus.CANCELLED,
                        reqOrderData.getPrice(), reqOrderData.getLeavesQty(), null, null, null));
                logger.info("Order with Id: {} has been removed by cancel message.", reqOrderData.getOrderId());
            }
            else {
                collector.add(new MEExecutionReport(generator.getNextExecutionId(), meCancelMessage.getClientId(),
                        meCancelMessage.getClientOrderId(), meCancelMessage.getOrderId(), ExecType.REJECTED, OrderStatus.REJECTED,
                        null, null, null, null, null));
                logger.error("Access to order cancellation is denied. Reason: real clientId: {}, submitted clientId: {}.",
                        reqOrderData.getClientId(), meCancelMessage.getClientId());
            }
        }
        // If orderId was incorrect or doesnt' exist
        else {
            collector.add(new MEExecutionReport(generator.getNextExecutionId(), meCancelMessage.getClientId(),
                    meCancelMessage.getClientOrderId(), meCancelMessage.getOrderId(), ExecType.REJECTED, OrderStatus.REJECTED,
                    null, null, null, null, null));
            logger.error("ClientOrderId: {} for order-cancellation doesn't exist.", meCancelMessage.getOriginalClientOrderId());
        }
    }


    public void process(MENewOrderMessage newOrderMessage, TransactionBuilder collector) {

        if (!isValid(newOrderMessage)) {
            logger.error("Incoming order with Id: {} was rejected by validation.", newOrderMessage.getOrderId());
            collector.add(new MEExecutionReport(generator.getNextExecutionId(), newOrderMessage.getClientId(),
                    newOrderMessage.getClientOrderId(), newOrderMessage.getOrderId(), ExecType.REJECTED,
                    OrderStatus.REJECTED, newOrderMessage.getLimitPrice(), newOrderMessage.getOrderQty(), null, null, null));
            return;
        }

        // If it stop or stop limit order add it to list till stop price will be suitable
        if (newOrderMessage.getOrderType() == OrderType.STOP_LIMIT || newOrderMessage.getOrderType() == OrderType.STOP) {
            if (newOrderMessage.getSide() == OrderSide.BID)
                stopBids.add(newOrderMessage);
            else
                stopOffers.add(newOrderMessage);
        } else {
            // Check for Stop & Stop Limit orders that can be added to Limit orders
            List<MENewOrderMessage> orderMessages = new ArrayList<>();
            if (!offers.isEmpty() && !stopOffers.isEmpty()) {
                BigDecimal borderPriceForOffers = offers.stream().min(Comparator.comparing(OrderData::getPrice)).get().getPrice();
                orderMessages = stopOffers.stream()
                        .filter(meNewOrderMessage -> meNewOrderMessage.getStopPrice().compareTo(borderPriceForOffers) >= 0)
                        .collect(Collectors.toList());
            }
            if (!bids.isEmpty()  && !stopBids.isEmpty()) {
                BigDecimal borderPriceForBids = bids.stream().max(Comparator.comparing(OrderData::getPrice)).get().getPrice();
                orderMessages.addAll(stopBids.stream()
                        .filter(meNewOrderMessage -> meNewOrderMessage.getStopPrice().compareTo(borderPriceForBids) <= 0)
                        .collect(Collectors.toList()));
            }
            orderMessages.add(newOrderMessage);
            for (MENewOrderMessage message : orderMessages) {
                OrderData tempOrderData = null;
                OrderType orderType;
                switch (message.getOrderType()){
                    case STOP_LIMIT:
                        orderType = OrderType.LIMIT;
                        break;
                    case STOP:
                        orderType = OrderType.MARKET;
                        break;
                    default:
                        orderType = message.getOrderType();
                        break;
                }
                OrderTimeQualifier orderTimeQualifier = message.getTif();
                OrderSide orderSide = message.getSide();
                List<OrderData> orderList;
                if (orderSide == OrderSide.BID){
                    orderList = offers;
                }
                else {
                    orderList = bids;
                }

                OrderData incomingOrder = new OrderData(message.getInstrId(), generator.getNextTransactionId(), message.getClientId(),
                        message.getClientOrderId(), message.getOrderId(), orderSide, message.getLimitPrice(),
                        message.getOrderQty(), message.getOrderQty());

                while (incomingOrder.getLeavesQty().compareTo(minSize) > 0) {
                    // transactionId acts as time counter
                    Comparator<OrderData> priceComparator = Comparator.comparing(OrderData::getPrice);
                    if (orderSide == OrderSide.OFFER){
                        priceComparator = priceComparator.reversed();
                    }

                    List<OrderData> sortedOrderList = orderList.stream()
                            .sorted(priceComparator.thenComparing(OrderData::getTransactionId)).collect(Collectors.toList());

                    if (!sortedOrderList.isEmpty())
                        tempOrderData = findSuitablePair(orderType, incomingOrder, sortedOrderList, orderTimeQualifier);
                    else
                        logger.info("The opposite list of orders is empty.");

                    processingOfOrders(collector, tempOrderData, incomingOrder, orderType, orderTimeQualifier);

                    if (tempOrderData == null)
                        break;
                }
                break;
            }
        }
    }

    private OrderData findSuitablePair(OrderType orderType, OrderData incomingOrder, List<OrderData> ordersList, OrderTimeQualifier tif) {

        Predicate<OrderData> incomingOrderPriceGreaterThanOrderOnBook = orderData -> orderData.getPrice().compareTo(incomingOrder.getPrice()) <= 0;
        Predicate<OrderData> incomingOrderPriceLessThanOrderOnBook = orderData -> orderData.getPrice().compareTo(incomingOrder.getPrice()) >= 0;

        Predicate<OrderData> currentOrderDataPredicate;


        if (orderType == OrderType.STOP) {

            if (incomingOrder.getSide().equals(OrderSide.BID)) {
                currentOrderDataPredicate = incomingOrderPriceLessThanOrderOnBook;
            }
            else {
                currentOrderDataPredicate = incomingOrderPriceGreaterThanOrderOnBook;
            }
        }

        else if (orderType == OrderType.LIMIT) {

            if (incomingOrder.getSide().equals(OrderSide.BID)) {
                currentOrderDataPredicate = incomingOrderPriceGreaterThanOrderOnBook;
            }
            else {
                currentOrderDataPredicate = incomingOrderPriceLessThanOrderOnBook;
            }
        }
        // For Market order
        else {
            return ordersList.stream().findFirst().orElse(null);
        }
        if (tif == OrderTimeQualifier.FILL_OR_KILL){
            BigDecimal sumQty = ordersList.stream()
                    .filter(currentOrderDataPredicate)
                    .map(OrderData::getLeavesQty)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (sumQty.compareTo(incomingOrder.getLeavesQty()) < 0)
                return null;
        }
        return ordersList.stream().filter(currentOrderDataPredicate).findFirst().orElse(null);
    }

    private void processingOfOrders(TransactionBuilder collector, OrderData tempOrderData, OrderData incomingOrder,
                                    OrderType orderType, OrderTimeQualifier tif) {

        if (tempOrderData != null) {

            BigDecimal incomingQty = incomingOrder.getLeavesQty();
            BigDecimal existedQty = tempOrderData.getLeavesQty();
            BigDecimal executedQty = incomingQty.min(existedQty);
            tempOrderData.setLeavesQty(existedQty.subtract(incomingQty).max(BigDecimal.ZERO));
            incomingOrder.setLeavesQty(incomingQty.subtract(existedQty).max(BigDecimal.ZERO));

            OrderData buyOrder, sellOrder;

            if (incomingOrder.getSide() == OrderSide.BID){
                buyOrder = incomingOrder;
                sellOrder = tempOrderData;
            }
            else {
                buyOrder = tempOrderData;
                sellOrder = incomingOrder;
            }

            String matchId = generator.getNextMatchId();

            collector.add(new METradeMessage(matchId, buyOrder.getClientId(), sellOrder.getClientId(), buyOrder.getOrderId(),
                    sellOrder.getOrderId(), executedQty, tempOrderData.getPrice(), incomingOrder.getInstrumentId(), TradeType.REGULAR));
            logger.info("Successful trade between of buy-order: {} and sell-order: {}", buyOrder.getOrderId(), sellOrder.getOrderId());
            // Reports for incoming order
            // Filled
            if (incomingOrder.getLeavesQty().compareTo(BigDecimal.ZERO) == 0) {
                collector.add(new MEExecutionReport(generator.getNextExecutionId(), incomingOrder.getClientId(),
                        incomingOrder.getClientOrderId(), incomingOrder.getOrderId(), ExecType.TRADE, OrderStatus.FILLED,
                        incomingOrder.getPrice(), incomingOrder.getLeavesQty(), tempOrderData.getPrice(), executedQty, matchId));
            }
            // Partially filled
            else {
                collector.add(new MEExecutionReport(generator.getNextExecutionId(), incomingOrder.getClientId(),
                        incomingOrder.getClientOrderId(), incomingOrder.getOrderId(), ExecType.TRADE,
                        OrderStatus.PARTIALLY_FILLED,  incomingOrder.getPrice(), incomingOrder.getLeavesQty(),
                        tempOrderData.getPrice(), executedQty, matchId));
            }
            // Reports for existed order

            // Filled
            if (tempOrderData.getLeavesQty().equals(BigDecimal.ZERO)) {
                if (tempOrderData.getSide() == OrderSide.BID)
                    bids.remove(tempOrderData);
                else
                    offers.remove(tempOrderData);
                logger.info("Order with id: {} has been removed due to its completion.", tempOrderData.getOrderId());
                collector.add(new MEExecutionReport(generator.getNextExecutionId(), tempOrderData.getClientId(),
                        tempOrderData.getClientOrderId(), tempOrderData.getOrderId(), ExecType.TRADE, OrderStatus.FILLED,
                        tempOrderData.getPrice(), tempOrderData.getLeavesQty(), tempOrderData.getPrice(), existedQty, matchId));
            }
            // Partially filled
            else {
                collector.add(new MEExecutionReport(generator.getNextExecutionId(), tempOrderData.getClientId(),
                        tempOrderData.getClientOrderId(), tempOrderData.getOrderId(), ExecType.TRADE,
                        OrderStatus.PARTIALLY_FILLED, incomingOrder.getPrice(), incomingOrder.getLeavesQty(),
                        tempOrderData.getPrice(), executedQty, matchId));
                // Cancelled cause not enough qty for trading
                if (tempOrderData.getLeavesQty().compareTo(minSize) < 0) {
                    if (tempOrderData.getSide() == OrderSide.BID)
                        offers.remove(tempOrderData);
                    else
                        bids.remove(tempOrderData);
                    logger.info("Order with id: {} has been cancelled due to insufficient quantity.", tempOrderData.getOrderId());
                    collector.add(new MEExecutionReport(generator.getNextExecutionId(), tempOrderData.getClientId(),
                            tempOrderData.getClientOrderId(), tempOrderData.getOrderId(), ExecType.CANCELLED,
                            OrderStatus.CANCELLED, tempOrderData.getPrice(), tempOrderData.getLeavesQty(),
                            tempOrderData.getPrice(), tempOrderData.getLeavesQty(), null));
                }
            }
        }
        // No such offer
        else {

            if (orderType == OrderType.MARKET || tif == OrderTimeQualifier.IMMEDIATE_OR_CANCEL || tif == OrderTimeQualifier.FILL_OR_KILL) {
                logger.info("Order with id: {} has been cancelled because there was no suitable offer.", incomingOrder.getOrderId());
                collector.add(new MEExecutionReport(generator.getNextExecutionId(), incomingOrder.getClientId(),
                        incomingOrder.getClientOrderId(), incomingOrder.getOrderId(), ExecType.CANCELLED,
                        OrderStatus.CANCELLED, incomingOrder.getPrice(), incomingOrder.getLeavesQty(), null, null, null));
            }

            else {
                collector.add(new MEExecutionReport(generator.getNextExecutionId(), incomingOrder.getClientId(),
                        incomingOrder.getClientOrderId(), incomingOrder.getOrderId(), ExecType.NEW, OrderStatus.NEW,
                        incomingOrder.getPrice(), incomingOrder.getLeavesQty(), null, null, null));
                logger.info("Order with id: {} was placed to the OrderBook because there was no suitable offer.", incomingOrder.getOrderId());
                if (incomingOrder.getSide() == OrderSide.BID)
                    bids.add(incomingOrder);
                else
                    offers.add(incomingOrder);

            }
        }
    }

    public long getInstrumentId() {
        return instrumentId;
    }

    /* package */ List<OrderData> getBids() {
        return bids;
    }

    /* package */ List<OrderData> getOffers() {
        return offers;
    }

    private boolean isValid(MENewOrderMessage msg) {
        if (msg.getOrderQty().compareTo(minSize) < 0) {
            logger.error("Value of order's quantity less than minimal value. Minimal size: {}, submitted quantity: {}.",
                    minSize, msg.getOrderQty());
            return false;
        }
        ArrayList<OrderType> types = new ArrayList<>(Arrays.asList(OrderType.values()));
        if (!types.contains(msg.getOrderType())) {
            logger.error("Unexpected value of order's type: {}.", msg.getOrderType());
            return false;
        }
        ArrayList<OrderSide> sides = new ArrayList<>(Arrays.asList(OrderSide.values()));
        if (!sides.contains(msg.getSide())) {
            logger.error("Unexpected value of order's side: {}.", msg.getSide());
            return false;
        }
        ArrayList<OrderTimeQualifier> tif = new ArrayList<>(Arrays.asList(OrderTimeQualifier.values()));
        if (!tif.contains(msg.getTif())) {
            logger.error("Unexpected value of order's time in force type: {}.", msg.getTif());
            return false;
        }
        return true;
    }
}
