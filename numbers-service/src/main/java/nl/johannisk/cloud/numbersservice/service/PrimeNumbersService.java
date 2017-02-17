package nl.johannisk.cloud.numbersservice.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PrimeNumbersService {

    public List<Integer> calculatePrimeNumbers(int from, int to) {
        List<Integer> primeNumbers = new ArrayList<>();
        for (int i = from; i <= to; i++) {
            int counter = getDivisorsCount(i);
            if (counter == 2) {
                primeNumbers.add(i);
            }
        }
        
        return primeNumbers;
    }

    private int getDivisorsCount(int i) {
        int counter = 0;
        for (int num = i; num >= 1; num--) {
            if (i % num == 0) {
                counter++;
            }
        }
        return counter;
    }
}
