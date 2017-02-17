package nl.johannisk.cloud.frontend.client;

public class PrimeNumbersRequest {

    private final Integer from;
    private final Integer to;

    public PrimeNumbersRequest(Integer from, Integer to) {
        this.from = from;
        this.to = to;
    }

    public Integer getFrom() {
        return from;
    }

    public Integer getTo() {
        return to;
    }
}
