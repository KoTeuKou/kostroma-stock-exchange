package edu.students.kse.me;

import java.util.HashMap;
import java.util.Map;

public class OrderBookStorage {

    private final Map<Long, MEOrderBook> books = new HashMap<>();

    private MEIdGenerator generator;

    public OrderBookStorage(MEIdGenerator generator) {
        this.generator = generator;
    }

    public MEOrderBook getOrCreateOrderBook(long instrumentId) {
        return books.computeIfAbsent(instrumentId, (instrId) -> {
            return new MEOrderBook(instrId, generator);
        });
    }

    public MEOrderBook getOrderBook(long instrumentId) {
        return books.get(instrumentId);
    }
}
