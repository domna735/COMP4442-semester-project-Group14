package hk.polyu.comp4442.cloudcompute.dto;

import java.time.LocalDateTime;

public class CalculateResponse {

    private String expression;
    private double result;
    private LocalDateTime timestamp;

    public CalculateResponse(String expression, double result, LocalDateTime timestamp) {
        this.expression = expression;
        this.result = result;
        this.timestamp = timestamp;
    }

    public String getExpression() {
        return expression;
    }

    public double getResult() {
        return result;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
