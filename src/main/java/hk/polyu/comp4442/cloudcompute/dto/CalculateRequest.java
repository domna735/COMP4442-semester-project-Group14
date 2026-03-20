package hk.polyu.comp4442.cloudcompute.dto;

import jakarta.validation.constraints.NotNull;

public class CalculateRequest {

    @NotNull
    private Double operandA;

    @NotNull
    private Double operandB;

    @NotNull
    private Operator operator;

    public Double getOperandA() {
        return operandA;
    }

    public void setOperandA(Double operandA) {
        this.operandA = operandA;
    }

    public Double getOperandB() {
        return operandB;
    }

    public void setOperandB(Double operandB) {
        this.operandB = operandB;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public enum Operator {
        ADD,
        SUBTRACT,
        MULTIPLY,
        DIVIDE
    }
}
