package edu.students.kse.me;

import edu.students.kse.me.enums.OrderSide;
import edu.students.kse.me.messages.MECancelMessage;
import edu.students.kse.me.messages.MEExecutionReport;
import edu.students.kse.me.messages.MEInputMessage;
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


    public MEOrderBook(long instrumentId) {
        this.instrumentId = instrumentId;
    }

    public void process(MEInputMessage inputMessage, TransactionBuilder collector) {
        //FIXME: Replace empty strings and 0 values
        //TODO: Use TIF field
        MENewOrderMessage msg = null;
        if (inputMessage instanceof MENewOrderMessage) {
            msg = (MENewOrderMessage) inputMessage;
        }
        else if (inputMessage instanceof MECancelMessage){
            MECancelMessage cancelMessage = (MECancelMessage) inputMessage;
            if (cancelMessage.getSide() == 1) {
                removeOrderByCancelMessage(collector, cancelMessage, bids);
            }
            else {
                removeOrderByCancelMessage(collector, cancelMessage, offers);
            }
        }
        else {
            throw new IllegalArgumentException();
        }

        if (msg != null) {
            // Validation check
            if (isValid(msg)) {
                collector.add(new MEExecutionReport("", msg.getClientId(), msg.getClientOrderId(), msg.getRequestId(),
                        '0', '0', msg.getLimitPrice(), msg.getOrderQty()));
                // If it stop limit order add it to list till stop price will be suitable
                if (msg.getOrderType() == 4) {
                    if (msg.getSide() == 1)
                        stoppedLimitBids.add(msg);
                    else
                        stoppedLimitOffers.add(msg);
                } else {
                    // Check for Stop Limit orders that can be added to Limit orders
                    List<MENewOrderMessage> orderMessages = new ArrayList<>();
                    if (offers.size() != 0 && stoppedLimitOffers.size() != 0) {
                        BigDecimal borderPriceForOffers = offers.stream().min(Comparator.comparing(OrderData::getPrice)).get().getPrice();
                        orderMessages = stoppedLimitOffers.stream()
                                .filter(meNewOrderMessage -> meNewOrderMessage.getStopPrice().compareTo(borderPriceForOffers) >= 0).collect(Collectors.toList());
                    }
                    if (bids.size() != 0  && stoppedLimitBids.size() != 0) {
                        BigDecimal borderPriceForBids = bids.stream().min(Comparator.comparing(OrderData::getPrice)).get().getPrice();
                        orderMessages.addAll(stoppedLimitBids.stream()
                                .filter(meNewOrderMessage -> meNewOrderMessage.getStopPrice().compareTo(borderPriceForBids) >= 0).collect(Collectors.toList()));
                    }
                    orderMessages.add(msg);
                    for (MENewOrderMessage message : orderMessages) {
                        BigDecimal limitPrice = message.getLimitPrice();
                        OrderData tempOrderData = null;
                        byte orderType = message.getOrderType();

                        switch (message.getSide()) {
                            // Buy
                            case 1:
                                    OrderData incomingBuyOrder = new OrderData(message.getInstrId(), 0L, message.getOrderQty(), message.getDisplayQty(),
                                            message.getClientId(), message.getClientOrderId(), OrderSide.BID, message.getLimitPrice());
                                    List<OrderData> offersList = offers.stream()
                                            .sorted(Comparator.comparing(OrderData::getPrice)).collect(Collectors.toList());

                                    // Find suitable offer
                                    if (offersList.size() != 0)
                                        tempOrderData = findSuitablePair(limitPrice, orderType, incomingBuyOrder, offersList);

                                    if (orderType != 4)
                                        generateReportsForOrders(collector, tempOrderData, incomingBuyOrder, offers, bids, orderType);

                                break;

                            // Sell
                            case 2:

                                    OrderData incomingSellOrder = new OrderData(message.getInstrId(), 0L, message.getOrderQty(), message.getDisplayQty(),
                                            message.getClientId(), message.getClientOrderId(), OrderSide.OFFER, message.getLimitPrice());

                                    List<OrderData> bidsList = bids.stream()
                                            .sorted(Comparator.comparing(OrderData::getPrice).reversed()).collect(Collectors.toList());
                                    // Find suitable buyer
                                    if (bidsList.size() != 0)
                                        tempOrderData = findSuitablePair(limitPrice, orderType, incomingSellOrder, bidsList);
                                    // If such offer exists
                                    if (orderType != 4)
                                        generateReportsForOrders(collector, tempOrderData, incomingSellOrder, bids, offers, orderType);

                                break;
                        }
                    }
                }
            }
            else{
                collector.add(new MEExecutionReport("", msg.getClientId(), msg.getClientOrderId(), msg.getRequestId(),
                        '8', '8', msg.getLimitPrice(), msg.getOrderQty()));
            }
        }

        // orderType
        //        1 - Market
        //        2 - Limit
        //        3 - Stop
        //        4 - Stop Limit

        // execType
        //        0 - New
        //        4 - Cancelled
        //        8 - Rejected
        //        F - Trade

        // status
        //        0 - New
        //        1 - Partially filled
        //        2 - Filled
        //        4 - Cancelled
        //        8 - Rejected

        // tif
        //      1 - Good Till Cancel
        //      3 - Immediate or Cancel (IOC)
        //      4 - Fill or Kill (FOK)
    }

    private void removeOrderByCancelMessage(TransactionBuilder collector, MECancelMessage cancelMessage, ArrayList<OrderData> offers) {
        OrderData reqOrderData;
        reqOrderData = offers.stream()
                .filter(orderData -> orderData.getOrderId().equals(cancelMessage.getOriginalClientOrderId()))
                .findFirst().orElse(null);
        if (reqOrderData != null) {
            offers.remove(reqOrderData);
            collector.add(new MEExecutionReport("", cancelMessage.getClientId(), cancelMessage.getClientOrderId(),
                    cancelMessage.getOrderId(), '4', '4', reqOrderData.getPrice(), reqOrderData.getQty()));
        }
        else {
            collector.add(new MEExecutionReport("", cancelMessage.getClientId(), cancelMessage.getClientOrderId(),
                    cancelMessage.getOrderId(), '8', '8', null, null));
        }
    }

    private OrderData findSuitablePair(BigDecimal limitPrice, byte orderType, OrderData incomingOrder, List<OrderData> offersList) {
        Optional<OrderData> suitableOrder;
        // For Stop order
        if (orderType == 3) {
            suitableOrder = offersList.stream()
                    .filter(orderData -> orderData.getPrice().compareTo(limitPrice) >= 0 &&
                            orderData.getQty().compareTo(incomingOrder.getQty()) >= 0)
                    .findFirst();
        }
        // For other orders
        else {
            suitableOrder = offersList.stream()
                    .filter(orderData -> orderData.getPrice().compareTo(limitPrice) <= 0 &&
                            orderData.getQty().compareTo(incomingOrder.getQty()) >= 0)
                    .findFirst();
        }
        return suitableOrder.orElse(null);
    }

    private void generateReportsForOrders(TransactionBuilder collector, OrderData tempOrderData, OrderData incomingOrder,
                                          ArrayList<OrderData> offers, ArrayList<OrderData> bids, byte orderType) {
        // If such offer exists
        if (tempOrderData != null) {
            tempOrderData.setQty(tempOrderData.getQty().subtract(incomingOrder.getQty()));
            incomingOrder.setQty(incomingOrder.getQty().subtract(tempOrderData.getQty()));
            // Reports for incoming order
            // Filled
            if (incomingOrder.getQty().compareTo(new BigDecimal("0")) == 0){
                collector.add(new MEExecutionReport("", "", "", incomingOrder.getOrderId(),
                        'F', '2', incomingOrder.getPrice(), incomingOrder.getQty()));
            }
            // Partially filled
            else {
                collector.add(new MEExecutionReport("", "", "", incomingOrder.getOrderId(),
                        'F', '1', incomingOrder.getPrice(), incomingOrder.getQty()));
            }
            // Reports for existed order
            // Filled
            if (tempOrderData.getQty().equals(new BigDecimal("0"))) {
                offers.remove(tempOrderData);
                collector.add(new MEExecutionReport("", "", "", tempOrderData.getOrderId(),
                        'F', '2', tempOrderData.getPrice(), tempOrderData.getQty()));
            }
            // Partially filled
            else if (tempOrderData.getQty().compareTo(limitSize) >= 0) {
                collector.add(new MEExecutionReport("", "", "", tempOrderData.getOrderId(),
                        'F', '1', tempOrderData.getPrice(), tempOrderData.getQty()));
            }
            // Cancelled
            else {
                offers.remove(tempOrderData);
                collector.add(new MEExecutionReport("", "", "", tempOrderData.getOrderId(),
                        '4', '4', tempOrderData.getPrice(), tempOrderData.getQty()));
            }
        }
        // No such offer
        else {
            // If Market order so cancel it
            if (orderType == 1) {
                collector.add(new MEExecutionReport("", "", "", incomingOrder.getOrderId(),
                        '4', '4', incomingOrder.getPrice(), incomingOrder.getQty()));
            }
            // Else Limit, Stop, Stop-limit order, so add to book
            else {
                bids.add(incomingOrder);
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
        ArrayList<Byte> types = new ArrayList<>(Arrays.asList((byte) 1, (byte) 2, (byte) 3, (byte) 4));
        if (!types.contains(msg.getOrderType()))
            return false;
        ArrayList<Byte> sides = new ArrayList<>(Arrays.asList((byte) 1, (byte) 2));
        if (!sides.contains(msg.getSide()))
            return false;
        ArrayList<Byte> tif = new ArrayList<>(Arrays.asList((byte) 1, (byte) 3, (byte) 4));
        return tif.contains(msg.getTif());
    }

}


