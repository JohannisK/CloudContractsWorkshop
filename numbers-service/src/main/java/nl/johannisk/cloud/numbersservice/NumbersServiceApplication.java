package nl.johannisk.cloud.numbersservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class NumbersServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NumbersServiceApplication.class, args);
    }
}
