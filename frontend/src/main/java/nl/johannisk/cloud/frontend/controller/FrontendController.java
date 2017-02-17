package nl.johannisk.cloud.frontend.controller;

import nl.johannisk.cloud.frontend.client.NumberServiceClient;
import nl.johannisk.cloud.frontend.client.PrimeNumbersRequest;
import nl.johannisk.cloud.frontend.client.PrimeNumbersResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Controller
public class FrontendController {

    private final NumberServiceClient numberServiceClient;

    public FrontendController(NumberServiceClient numberServiceClient) {
        this.numberServiceClient = numberServiceClient;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String render(Model model) {
        NumbersForm form = new NumbersForm();
        form.setFrom(0);
        form.setTo(100);

        model.addAttribute("numbersform", form);

        return "home";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String submit(@ModelAttribute NumbersForm numbersform,
                         Model model) {
        model.addAttribute("numbersform", numbersform);

        Instant start = Instant.now();

        PrimeNumbersResponse primeNumbersResponse =
                numberServiceClient.calculatePrimeNumbers(
                        new PrimeNumbersRequest(numbersform.getFrom(), numbersform.getTo())
                );
        List<Integer> primeNumbers = primeNumbersResponse.getPrimeNumbers();
        String instanceId = primeNumbersResponse.getInstanceId();

        Instant end = Instant.now();

        model.addAttribute("primenumbers", primeNumbers);
        model.addAttribute("instanceId", instanceId);
        model.addAttribute("duration", Duration.between(start, end));

        return "home";
    }
}
