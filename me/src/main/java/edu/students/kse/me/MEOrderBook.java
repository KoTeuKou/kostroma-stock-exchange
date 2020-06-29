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

public class MEOrderBook {

    private final long instrumentId;
    private static final BigDecimal minSize = new BigDecimal("1000");
    private final ArrayList<OrderData> bids = new ArrayList<>();
    private final ArrayList<OrderData> offers = new ArrayList<>();
    private final ArrayList<MENewOrderMessage> stoppedLimitOffers = new ArrayList<>();
    private final ArrayList<MENewOrderMessage> stoppedLimitBids = new ArrayList<>();
    private final MEIdGenerator generator;


    public MEOrderBook(long instrumentId, MEIdGenerator generator) {
        this.instrumentId = instrumentId;
        this.generator = generator;
    }

    public void process(MECancelMessage meCancelMessage, TransactionBuilder collector){
        OrderData reqOrderData;
        ArrayList<OrderData> orders;
        if (meCancelMessage.getSide() == OrderSide.BID){
            orders = this.bids;
        }
        else {
            orders = offers;
        }
        reqOrderData = orders.stream()
                .filter(orderData -> orderData.getClientOrderId().equals(meCancelMessage.getOriginalClientOrderId()))
                .findFirst().orElse(null);
        // If such order has been found => remove from book & create report
        if (reqOrderData != null) {
            if (meCancelMessage.getSide() == OrderSide.BID) {
                this.bids.remove(reqOrderData);
            }
            else {
                offers.remove(reqOrderData);
            }
            collector.add(new MEExecutionReport(generator.getNextExecutionId(), meCancelMessage.getClientId(),
                    meCancelMessage.getClientOrderId(), reqOrderData.getOrderId(), ExecType.CANCELLED, OrderStatus.CANCELLED,
                    reqOrderData.getPrice(), reqOrderData.getLeavesQty(), null, null));
        }
        // If orderId was incorrect or doesnt' exist
        else {
            collector.add(new MEExecutionReport(generator.getNextExecutionId(), meCancelMessage.getClientId(),
                    meCancelMessage.getClientOrderId(), meCancelMessage.getOrderId(), ExecType.REJECTED, OrderStatus.REJECTED,
                    null, null, null, null));
        }
    }


    public void process(MENewOrderMessage newOrderMessage, TransactionBuilder collector) {

        if (!isValid(newOrderMessage)) {
            collector.add(new MEExecutionReport(generator.getNextExecutionId(), newOrderMessage.getClientId(),
                    newOrderMessage.getClientOrderId(), newOrderMessage.getOrderId(), ExecType.REJECTED,
                    OrderStatus.REJECTED, newOrderMessage.getLimitPrice(), newOrderMessage.getOrderQty(), null, null));
            return;
        }

        // If it stop limit order add it to list till stop price will be suitable
        if (newOrderMessage.getOrderType() == OrderType.STOP_LIMIT) {
            if (newOrderMessage.getSide() == OrderSide.BID)
                stoppedLimitBids.add(newOrderMessage);
            else
                stoppedLimitOffers.add(newOrderMessage);
        } else {
            // Check for Stop Limit orders that can be added to Limit orders
            List<MENewOrderMessage> orderMessages = new ArrayList<>();
            if (!offers.isEmpty() && !stoppedLimitOffers.isEmpty()) {
                BigDecimal borderPriceForOffers = offers.stream().min(Comparator.comparing(OrderData::getPrice)).get().getPrice();
                orderMessages = stoppedLimitOffers.stream()
                        .filter(meNewOrderMessage -> meNewOrderMessage.getStopPrice().compareTo(borderPriceForOffers) >= 0)
                        .collect(Collectors.toList());
            }
            if (!bids.isEmpty()  && !stoppedLimitBids.isEmpty()) {
                BigDecimal borderPriceForBids = bids.stream().max(Comparator.comparing(OrderData::getPrice)).get().getPrice();
                orderMessages.addAll(stoppedLimitBids.stream()
                        .filter(meNewOrderMessage -> meNewOrderMessage.getStopPrice().compareTo(borderPriceForBids) <= 0)
                        .collect(Collectors.toList()));
            }
            orderMessages.add(newOrderMessage);
            for (MENewOrderMessage message : orderMessages) {
                OrderData tempOrderData = null;
                OrderType orderType = message.getOrderType() == OrderType.STOP_LIMIT ? OrderType.LIMIT : message.getOrderType();
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

                    if (orderType != OrderType.STOP_LIMIT)
                        generateReportsForOrders(collector, tempOrderData, incomingOrder, orderType, orderTimeQualifier);

                    if (tempOrderData == null)
                        break;
                }
                break;
            }
        }
    }

    private OrderData findSuitablePair(OrderType orderType, OrderData incomingOrder, List<OrderData> ordersList, OrderTimeQualifier tif) {

        Predicate<OrderData> orderDataPredicate = orderData -> orderData.getPrice().compareTo(incomingOrder.getPrice()) <= 0;
        Predicate<OrderData> anotherOrderDataPredicate = orderData -> orderData.getPrice().compareTo(incomingOrder.getPrice()) >= 0;
        Predicate<OrderData> predicateFOK = orderData -> orderData.getLeavesQty().compareTo(incomingOrder.getLeavesQty()) >= 0;

        Predicate<OrderData> currentOrderDataPredicate;

        if (orderType == OrderType.STOP) {

            if (incomingOrder.getSide().equals(OrderSide.BID)) {
                currentOrderDataPredicate = anotherOrderDataPredicate;
            }
            else {
                currentOrderDataPredicate = orderDataPredicate;
            }
        }

        else if (orderType == OrderType.LIMIT) {

            if (incomingOrder.getSide().equals(OrderSide.BID)) {
                currentOrderDataPredicate = orderDataPredicate;
            }
            else {
                currentOrderDataPredicate = anotherOrderDataPredicate;
            }
        }
        // For Market order
        else {
            if (tif == OrderTimeQualifier.FILL_OR_KILL)
                currentOrderDataPredicate = predicateFOK;
            else
                return ordersList.stream().findFirst().orElse(null);
        }

        if (tif == OrderTimeQualifier.FILL_OR_KILL){
            currentOrderDataPredicate = currentOrderDataPredicate.and(predicateFOK);
        }
        return ordersList.stream().filter(currentOrderDataPredicate).findFirst().orElse(null);
    }

    private void generateReportsForOrders(TransactionBuilder collector, OrderData tempOrderData, OrderData incomingOrder,
                                          OrderType orderType, OrderTimeQualifier tif) {

        if (tempOrderData != null) {

            BigDecimal incomingQty = incomingOrder.getLeavesQty();
            BigDecimal existedQty = tempOrderData.getLeavesQty();

            tempOrderData.setLeavesQty(existedQty.subtract(incomingQty).max(BigDecimal.ZERO));
            incomingOrder.setLeavesQty(incomingQty.subtract(existedQty).max(BigDecimal.ZERO));

            // Reports for incoming order

            // Filled
            if (incomingOrder.getLeavesQty().compareTo(BigDecimal.ZERO) == 0) {
                collector.add(new MEExecutionReport(generator.getNextExecutionId(), incomingOrder.getClientId(),
                        incomingOrder.getClientOrderId(), incomingOrder.getOrderId(), ExecType.TRADE, OrderStatus.FILLED,
                        incomingOrder.getPrice(), incomingOrder.getLeavesQty(), tempOrderData.getPrice(), incomingQty));
            }
            // Partially filled
            else {
                collector.add(new MEExecutionReport(generator.getNextExecutionId(), incomingOrder.getClientId(),
                        incomingOrder.getClientOrderId(), incomingOrder.getOrderId(), ExecType.TRADE,
                        OrderStatus.PARTIALLY_FILLED,  incomingOrder.getPrice(), incomingOrder.getLeavesQty(),
                        tempOrderData.getPrice(), incomingQty.subtract(incomingOrder.getLeavesQty())));
            }
            // Reports for existed order

            // Filled
            if (tempOrderData.getLeavesQty().equals(BigDecimal.ZERO)) {
                if (tempOrderData.getSide() == OrderSide.BID)
                    bids.remove(tempOrderData);
                else
                    offers.remove(tempOrderData);

                collector.add(new MEExecutionReport(generator.getNextExecutionId(), tempOrderData.getClientId(),
                        tempOrderData.getClientOrderId(), tempOrderData.getOrderId(), ExecType.TRADE, OrderStatus.FILLED,
                        tempOrderData.getPrice(), tempOrderData.getLeavesQty(), tempOrderData.getPrice(), existedQty));
            }
            // Partially filled
            else if (tempOrderData.getLeavesQty().compareTo(minSize) >= 0) {
                collector.add(new MEExecutionReport(generator.getNextExecutionId(), tempOrderData.getClientId(),
                        tempOrderData.getClientOrderId(), tempOrderData.getOrderId(), ExecType.TRADE,
                        OrderStatus.PARTIALLY_FILLED, incomingOrder.getPrice(), incomingOrder.getLeavesQty(),
                        tempOrderData.getPrice(), existedQty.subtract(tempOrderData.getLeavesQty())));
            }
            // Cancelled cause not enough qty for trading
            else {
                if (tempOrderData.getSide() == OrderSide.BID)
                    offers.remove(tempOrderData);
                else
                    bids.remove(tempOrderData);

                collector.add(new MEExecutionReport(generator.getNextExecutionId(), tempOrderData.getClientId(),
                        tempOrderData.getClientOrderId(), tempOrderData.getOrderId(), ExecType.CANCELLED,
                        OrderStatus.CANCELLED, tempOrderData.getPrice(), tempOrderData.getLeavesQty(),
                        tempOrderData.getPrice(), tempOrderData.getLeavesQty()));
            }
        }
        // No such offer
        else {

            if (orderType == OrderType.MARKET || tif == OrderTimeQualifier.IMMEDIATE_OR_CANCEL) {
                collector.add(new MEExecutionReport(generator.getNextExecutionId(), incomingOrder.getClientId(),
                        incomingOrder.getClientOrderId(), incomingOrder.getOrderId(), ExecType.CANCELLED,
                        OrderStatus.CANCELLED, incomingOrder.getPrice(), incomingOrder.getLeavesQty(), null, null));
            }

            else {
                collector.add(new MEExecutionReport(generator.getNextExecutionId(), incomingOrder.getClientId(),
                        incomingOrder.getClientOrderId(), incomingOrder.getOrderId(), ExecType.NEW, OrderStatus.NEW,
                        incomingOrder.getPrice(), incomingOrder.getLeavesQty(), null, null));

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
        if (msg.getOrderQty().compareTo(minSize) < 0)
            return false;
        ArrayList<OrderType> types = new ArrayList<>(Arrays.asList(OrderType.values()));
        if (!types.contains(msg.getOrderType()))
            return false;
        ArrayList<OrderSide> sides = new ArrayList<>(Arrays.asList(OrderSide.values()));
        if (!sides.contains(msg.getSide()))
            return false;
        ArrayList<OrderTimeQualifier> tif = new ArrayList<>(Arrays.asList(OrderTimeQualifier.values()));
        return tif.contains(msg.getTif());
    }
}
