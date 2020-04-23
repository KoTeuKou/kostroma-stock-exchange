package edu.students.kse.me;

import edu.students.kse.me.messages.MEExecutionReport;
import edu.students.kse.me.messages.MENewOrderMessage;

import java.util.*;

public class MEOrderBook {

    private final long instrumentId;

    private final ArrayList<OrderData> bids = new ArrayList<>();
    private final ArrayList<OrderData> offers = new ArrayList<>();

    public MEOrderBook(long instrumentId) {
        this.instrumentId = instrumentId;
    }

    public void process(MENewOrderMessage msg, TransactionBuilder collector) {
        // FIXME: add logic here
        // converting message
        collector.add(new MEExecutionReport());
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

}
