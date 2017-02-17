package nl.johannisk.cloud.frontend.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PrimeNumbersResponse {

    private final List<Integer> primeNumbers;
    private final String instanceId;

    @JsonCreator
    public PrimeNumbersResponse(@JsonProperty("primeNumbers") List<Integer> primeNumbers, @JsonProperty("instanceId") String instanceId) {
        this.primeNumbers = primeNumbers;
        this.instanceId = instanceId;
    }

    public List<Integer> getPrimeNumbers() {
        return primeNumbers;
    }

    public String getInstanceId() {
        return instanceId;
    }
}
