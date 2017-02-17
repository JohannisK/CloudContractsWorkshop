package nl.johannisk.cloud.numbersservice.controller;

import com.netflix.appinfo.EurekaInstanceConfig;
import nl.johannisk.cloud.numbersservice.service.PrimeNumbersService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/primenumbers")
public class PrimeNumbersController {

    private final PrimeNumbersService primeNumbersService;

    private final EurekaInstanceConfig instanceConfig;

    public PrimeNumbersController(PrimeNumbersService primeNumbersService, EurekaInstanceConfig instanceConfig) {
        this.primeNumbersService = primeNumbersService;
        this.instanceConfig = instanceConfig;
    }

    @RequestMapping(method = RequestMethod.POST)
    public PrimeNumbersResponse calculatePrimeNumbers(@RequestBody @Validated PrimeNumbersRequest primeNumbersRequest) {
        List<Integer> primeNumbers = primeNumbersService.calculatePrimeNumbers(primeNumbersRequest.getFrom(), primeNumbersRequest.getTo());
        return new PrimeNumbersResponse(primeNumbers, instanceConfig.getInstanceId());
    }

}
