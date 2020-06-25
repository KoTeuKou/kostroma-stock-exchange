package edu.students.kse.me;

import edu.students.kse.me.enums.*;
import edu.students.kse.me.messages.MECancelMessage;
import edu.students.kse.me.messages.MEExecutionReport;
import edu.students.kse.me.messages.MENewOrderMessage;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class MEOrderBook {

    private final long instrumentId;
    private static final BigDecimal limitSize = new BigDecimal("1000");
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
        if (meCancelMessage.getSide() == OrderSide.BID) {
            removeOrderByCancelMessage(collector, meCancelMessage);
        }
        else {
            removeOrderByCancelMessage(collector, meCancelMessage);
        }
    }


    public void process(MENewOrderMessage newOrderMessage, TransactionBuilder collector) {

            if (!isValid(newOrderMessage)) {
                collector.add(new MEExecutionReport(generator.getNextExecutionId(), newOrderMessage.getClientId(),
                        newOrderMessage.getClientOrderId(), newOrderMessage.getOrderId(), ExecType.REJECTED,
                        OrderStatus.REJECTED, newOrderMessage.getLimitPrice(), newOrderMessage.getOrderQty(), null, null));
            }
            else{
                collector.add(new MEExecutionReport(generator.getNextExecutionId(), newOrderMessage.getClientId(),
                        newOrderMessage.getClientOrderId(), newOrderMessage.getOrderId(), ExecType.NEW, OrderStatus.NEW,
                        newOrderMessage.getLimitPrice(), newOrderMessage.getOrderQty(), null, null));
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
                        BigDecimal borderPriceForBids = bids.stream().min(Comparator.comparing(OrderData::getPrice)).get().getPrice();
                        orderMessages.addAll(stoppedLimitBids.stream()
                                .filter(meNewOrderMessage -> meNewOrderMessage.getStopPrice().compareTo(borderPriceForBids) >= 0)
                                .collect(Collectors.toList()));
                    }
                    orderMessages.add(newOrderMessage);
                    for (MENewOrderMessage message : orderMessages) {
                        BigDecimal limitPrice = message.getLimitPrice();
                        OrderData tempOrderData = null;
                        OrderType orderType = message.getOrderType();
                        OrderTimeQualifier orderTimeQualifier = message.getTif();

                        switch (message.getSide()) {
                            // Buy
                            case BID:
                                OrderData incomingBuyOrder = new OrderData(message.getInstrId(), generator.getNextTransactionId(), message.getClientId(),
                                        message.getClientOrderId(), message.getOrderId(), OrderSide.BID, message.getLimitPrice(),
                                        message.getOrderQty(), message.getDisplayQty());
                                // transactionId acts as time counter
                                List<OrderData> offersList = offers.stream()
                                        .sorted(Comparator.comparing(OrderData::getPriceForUnit).thenComparing(OrderData::getTransactionId)).collect(Collectors.toList());

                                if (!offersList.isEmpty())
                                    tempOrderData = findSuitablePair(limitPrice, orderType, incomingBuyOrder, offersList, orderTimeQualifier);

                                if (orderType != OrderType.STOP_LIMIT)
                                    generateReportsForOrders(collector, tempOrderData, incomingBuyOrder, orderType);

                                break;

                            // Sell
                            case OFFER:
                                OrderData incomingSellOrder = new OrderData(message.getInstrId(), generator.getNextTransactionId(), message.getClientId(),
                                        message.getClientOrderId(), message.getOrderId(), OrderSide.OFFER, message.getLimitPrice(),
                                        message.getOrderQty(), message.getDisplayQty());

                                List<OrderData> bidsList = bids.stream()
                                        .sorted(Comparator.comparing(OrderData::getPriceForUnit).reversed().thenComparing(OrderData::getTransactionId)).collect(Collectors.toList());

                                if (!bidsList.isEmpty())
                                    tempOrderData = findSuitablePair(limitPrice, orderType, incomingSellOrder, bidsList, orderTimeQualifier);

                                if (orderType != OrderType.STOP_LIMIT)
                                    generateReportsForOrders(collector, tempOrderData, incomingSellOrder, orderType);

                                break;
                        }
                    }
                }
            }
    }

    private void removeOrderByCancelMessage(TransactionBuilder collector, MECancelMessage cancelMessage) {
        OrderData reqOrderData;
        if (cancelMessage.getSide() == OrderSide.BID){
            reqOrderData = bids.stream()
                    .filter(orderData -> orderData.getClientOrderId().equals(cancelMessage.getOriginalClientOrderId()))
                    .findFirst().orElse(null);
        }
        else {
            reqOrderData = offers.stream()
                    .filter(orderData -> orderData.getClientOrderId().equals(cancelMessage.getOriginalClientOrderId()))
                    .findFirst().orElse(null);
        }
        // If such order has been found => remove from book & create report
        if (reqOrderData != null) {
            if (cancelMessage.getSide() == OrderSide.BID) {
                bids.remove(reqOrderData);
            }
            else {
                offers.remove(reqOrderData);
            }
            collector.add(new MEExecutionReport(generator.getNextExecutionId(), cancelMessage.getClientId(), cancelMessage.getClientOrderId(),
                    reqOrderData.getOrderId(), ExecType.CANCELLED, OrderStatus.CANCELLED, reqOrderData.getPrice(), reqOrderData.getQty(), null, null));
        }
        // If orderId was incorrect or doesnt' exist
        else {
            collector.add(new MEExecutionReport(generator.getNextExecutionId(), cancelMessage.getClientId(), cancelMessage.getClientOrderId(),
                    cancelMessage.getOrderId(), ExecType.REJECTED, OrderStatus.REJECTED, null, null, null, null));
        }
    }

    private OrderData findSuitablePair(BigDecimal limitPrice, OrderType orderType, OrderData incomingOrder, List<OrderData> ordersList, OrderTimeQualifier tif) {
        Optional<OrderData> suitableOrder;

        if (orderType == OrderType.STOP) {

            if (incomingOrder.getSide().equals(OrderSide.BID)) {
                suitableOrder = ordersList.stream()
                        .filter(orderData -> orderData.getPrice().compareTo(limitPrice) >= 0 &&
                                orderData.getQty().compareTo(incomingOrder.getQty()) >= 0)
                        .findFirst();
            }

            else {
                suitableOrder = ordersList.stream()
                        .filter(orderData -> orderData.getPrice().compareTo(limitPrice) >= 0 &&
                                orderData.getQty().compareTo(incomingOrder.getQty()) >= 0)
                        .findFirst();
            }
        }

        // For Market order
        else {

                if (orderType == OrderType.LIMIT && tif == OrderTimeQualifier.FILL_OR_KILL){
                    suitableOrder = ordersList.stream()
                            .filter(orderData -> orderData.getPrice().compareTo(limitPrice) <= 0 &&
                                    orderData.getQty().compareTo(incomingOrder.getQty()) >= 0)
                            .findFirst();
                }
                else {
                    suitableOrder = ordersList.stream()
                            .filter(orderData -> orderData.getPrice().compareTo(limitPrice) <= 0)
                            .findFirst();
                }
        }
        return suitableOrder.orElse(null);
    }

    private void generateReportsForOrders(TransactionBuilder collector, OrderData tempOrderData, OrderData incomingOrder, OrderType orderType) {
        if (tempOrderData != null) {
            BigDecimal incomingQty = incomingOrder.getQty();
            BigDecimal existedQty = tempOrderData.getQty();
            tempOrderData.setQty(existedQty.subtract(incomingQty).compareTo(new BigDecimal("0")) > 0?  existedQty.subtract(incomingQty) : new BigDecimal("0"));
            incomingOrder.setQty(incomingQty.subtract(existedQty).compareTo(new BigDecimal("0")) > 0?  incomingQty.subtract(existedQty) : new BigDecimal("0"));
            // Reports for incoming order

            // Filled
            if (incomingOrder.getQty().compareTo(new BigDecimal("0")) == 0) {
                collector.add(new MEExecutionReport(generator.getNextExecutionId(), incomingOrder.getClientId(),
                        incomingOrder.getClientOrderId(), incomingOrder.getOrderId(), ExecType.TRADE,
                        OrderStatus.FILLED, incomingOrder.getPrice(), incomingOrder.getQty(), tempOrderData.getPrice(), incomingQty));
            }
            // Partially filled
            else {
                collector.add(new MEExecutionReport(generator.getNextExecutionId(), incomingOrder.getClientId(),
                        incomingOrder.getClientOrderId(), incomingOrder.getOrderId(), ExecType.TRADE,
                        OrderStatus.PARTIALLY_FILLED,  incomingOrder.getPrice(), incomingOrder.getQty(), tempOrderData.getPrice(), incomingQty.subtract(incomingOrder.getQty())));
            }
            // Reports for existed order

            // Filled
            if (tempOrderData.getQty().equals(new BigDecimal("0"))) {
                if (tempOrderData.getSide() == OrderSide.BID)
                    offers.remove(tempOrderData);
                else
                    bids.remove(tempOrderData);

                collector.add(new MEExecutionReport(generator.getNextExecutionId(), tempOrderData.getClientId(),
                        tempOrderData.getClientOrderId(), tempOrderData.getOrderId(), ExecType.TRADE,
                        OrderStatus.FILLED,  tempOrderData.getPrice(), tempOrderData.getQty(), tempOrderData.getPrice(), existedQty));
            }
            // Partially filled
            else if (tempOrderData.getQty().compareTo(limitSize) >= 0) {
                collector.add(new MEExecutionReport(generator.getNextExecutionId(), tempOrderData.getClientId(),
                        tempOrderData.getClientOrderId(), tempOrderData.getOrderId(), ExecType.TRADE,
                        OrderStatus.PARTIALLY_FILLED, incomingOrder.getPrice(), incomingOrder.getQty(), tempOrderData.getPrice(), existedQty.subtract(tempOrderData.getQty())));
            }
            // Cancelled cause not enough qty for trading
            else {
                if (tempOrderData.getSide() == OrderSide.BID)
                    offers.remove(tempOrderData);
                else
                    bids.remove(tempOrderData);

                collector.add(new MEExecutionReport(generator.getNextExecutionId(), tempOrderData.getClientId(),
                        tempOrderData.getClientOrderId(), tempOrderData.getOrderId(), ExecType.CANCELLED,
                        OrderStatus.CANCELLED, tempOrderData.getPrice(), tempOrderData.getQty(),
                        tempOrderData.getPrice(), tempOrderData.getQty()));
            }
        }
        // No such offer
        else {

            if (orderType == OrderType.MARKET) {
                collector.add(new MEExecutionReport(generator.getNextExecutionId(), incomingOrder.getClientId(),
                        incomingOrder.getClientOrderId(), incomingOrder.getOrderId(), ExecType.CANCELLED,
                        OrderStatus.CANCELLED, incomingOrder.getPrice(), incomingOrder.getQty(), null, null));
            }

            else {
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
        if (msg.getOrderQty().compareTo(limitSize) < 0)
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


