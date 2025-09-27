package com.minisql.model;

import java.util.LinkedHashMap;
import java.util.Map;

// Represents a single row of data using a map to be flexible with column names.
public class Row {
    private final Map<String, Object> data;

    public Row() {
        this.data = new LinkedHashMap<>(); // Preserve insertion order
    }

    public void addValue(String column, Object value) {
        data.put(column, value);
    }

    public Object getValue(String column) {
        return data.get(column);
    }

    public Map<String, Object> getData() {
        return data;
    }

    @Override
    public String toString() {
        return data.toString();
    }
}
