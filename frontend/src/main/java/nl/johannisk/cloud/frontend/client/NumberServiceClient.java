package nl.johannisk.cloud.frontend.client;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NumberServiceClient {

    private final RestTemplate restTemplate;

    public NumberServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public PrimeNumbersResponse calculatePrimeNumbers(PrimeNumbersRequest primeNumbersRequest) {
        return restTemplate.postForObject("http://numbers-service/primenumbers",
                primeNumbersRequest,
                PrimeNumbersResponse.class);
    }

    //TODO 3.6
}
