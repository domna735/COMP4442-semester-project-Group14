package hk.polyu.comp4442.cloudcompute.service;

import hk.polyu.comp4442.cloudcompute.dto.CalculateRequest;
import hk.polyu.comp4442.cloudcompute.dto.CalculateResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ComputeService {

    public CalculateResponse calculate(CalculateRequest request) {
        double a = request.getOperandA();
        double b = request.getOperandB();
        double result;
        String symbol;

        switch (request.getOperator()) {
            case ADD -> {
                result = a + b;
                symbol = "+";
            }
            case SUBTRACT -> {
                result = a - b;
                symbol = "-";
            }
            case MULTIPLY -> {
                result = a * b;
                symbol = "*";
            }
            case DIVIDE -> {
                if (b == 0.0d) {
                    throw new IllegalArgumentException("Division by zero is not allowed.");
                }
                result = a / b;
                symbol = "/";
            }
            default -> throw new IllegalArgumentException("Unsupported operator.");
        }

        String expression = a + " " + symbol + " " + b;
        return new CalculateResponse(expression, result, LocalDateTime.now());
    }
}
