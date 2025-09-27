package com.minisql.model;

import java.util.List;

// Represents a parsed SQL query.
public class Query {
    private final List<String> columns;
    private final String tableName;
    private final FilterCondition condition;

    public Query(List<String> columns, String tableName, FilterCondition condition) {
        this.columns = columns;
        this.tableName = tableName;
        this.condition = condition;
    }

    public List<String> getColumns() {
        return columns;
    }

    public boolean hasSelectAll() {
        return columns.size() == 1 && columns.get(0).equals("*");
    }

    public FilterCondition getCondition() {
        return condition;
    }

    public boolean hasCondition() {
        return condition != null;
    }
}
