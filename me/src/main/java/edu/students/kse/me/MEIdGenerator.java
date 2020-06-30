package edu.students.kse.me;

public class MEIdGenerator {

    private long id = 0L;

    public long getNextId() {
        return id++;
    }

    public String getNextOrderId() {
        return "O" + id++;
    }

    public String getNextMatchId() {
        return "M" + id++;
    }

    public String getNextExecutionId() {
        return "E" + id++;
    }

    public long getNextTransactionId() {return id++;}
}
