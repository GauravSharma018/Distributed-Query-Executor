package com.minisql;

import com.minisql.model.Query;
import com.minisql.model.Row;

import java.util.*;

// Represents a worker node responsible for a partition of the data.
public class Worker {
    private final int id;
    private final List<Row> dataPartition;
    // Basic Indexing: Map<ColumnName, Map<Value, List<RowIndex>>>
    private final Map<String, Map<Object, List<Integer>>> indexes = new HashMap<>();

    public Worker(int id, List<Row> dataPartition) {
        this.id = id;
        this.dataPartition = dataPartition;
        System.out.println("Worker " + id + " initialized with " + dataPartition.size() + " rows.");
    }

    // Creates an index on a given column for faster lookups.
    public void createIndex(String columnName) {
        Map<Object, List<Integer>> index = new HashMap<>();
        for (int i = 0; i < dataPartition.size(); i++) {
            Object value = dataPartition.get(i).getValue(columnName);
            index.computeIfAbsent(value, k -> new ArrayList<>()).add(i);
        }
        indexes.put(columnName, index);
        System.out.println("Worker " + id + " created index on column '" + columnName + "'.");
    }

    // Executes a sub-query on this worker's data partition.
    public List<Row> executeSubQuery(Query query) {
        List<Row> results = new ArrayList<>();
        List<Row> rowsToScan = getRowsUsingIndex(query);

        // Scan the relevant rows (either from index or full partition)
        for (Row row : rowsToScan) {
            if (!query.hasCondition() || query.getCondition().evaluate(row)) {
                results.add(projectColumns(row, query));
            }
        }
        return results;
    }

    // Tries to get a smaller set of rows to scan by using an index if available.
    private List<Row> getRowsUsingIndex(Query query) {
        if (query.hasCondition() && indexes.containsKey(query.getCondition().getColumn())) {
            Map<Object, List<Integer>> index = indexes.get(query.getCondition().getColumn());
            // Note: This is a simplified index lookup for '='. A real engine
            // would use B-trees for range scans (> , <).
            if (query.getCondition().getOperator().equals("=")) {
                Object value = query.getCondition().getValue();
                if (index.containsKey(value)) {
                    List<Row> indexedRows = new ArrayList<>();
                    for (Integer rowIndex : index.get(value)) {
                        indexedRows.add(dataPartition.get(rowIndex));
                    }
                    return indexedRows;
                } else {
                    return Collections.emptyList(); // Value not in index, no results
                }
            }
        }
        // If no suitable index, fall back to a full scan of the partition
        return dataPartition;
    }

    // Filters a row to include only the columns specified in the SELECT clause.
    private Row projectColumns(Row row, Query query) {
        if (query.hasSelectAll()) {
            return row;
        }
        Row projectedRow = new Row();
        for (String column : query.getColumns()) {
            projectedRow.addValue(column, row.getValue(column));
        }
        return projectedRow;
    }
}
