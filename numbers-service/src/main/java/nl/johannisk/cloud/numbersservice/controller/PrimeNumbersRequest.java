package nl.johannisk.cloud.numbersservice.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class PrimeNumbersRequest {

    private final Integer from;
    private final Integer to;

    @JsonCreator
    public PrimeNumbersRequest(@JsonProperty("from") Integer from,
                               @JsonProperty("to") Integer to) {
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
