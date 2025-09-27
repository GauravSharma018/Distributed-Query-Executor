package com.minisql.model;

// Represents the condition in a WHERE clause, e.g., "age > 30"
public class FilterCondition {
    private final String column;
    private final String operator;
    private final Object value;

    public FilterCondition(String column, String operator, Object value) {
        this.column = column;
        this.operator = operator;
        this.value = value;
    }

    public String getColumn() {
        return column;
    }

    public String getOperator() {
        return operator;
    }

    public Object getValue() {
        return value;
    }

    // Evaluates this condition against a given row.
    public boolean evaluate(Row row) {
        Object rowValue = row.getValue(column);
        if (rowValue == null) {
            return false;
        }

        // Simple comparison for numbers and strings
        if (rowValue instanceof Number && value instanceof Number) {
            double rowNum = ((Number) rowValue).doubleValue();
            double condNum = ((Number) value).doubleValue();
            switch (operator) {
                case ">": return rowNum > condNum;
                case "<": return rowNum < condNum;
                case "=": return rowNum == condNum;
                default: return false;
            }
        } else if (rowValue instanceof String && value instanceof String) {
            return operator.equals("=") && rowValue.equals(value);
        }
        return false;
    }
}
