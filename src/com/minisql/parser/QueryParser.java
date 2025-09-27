package com.minisql.parser;

import com.minisql.model.FilterCondition;
import com.minisql.model.Query;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// A simple parser for "SELECT ... FROM ... WHERE ..." queries.
public class QueryParser {
    // Regex to capture SELECT columns, FROM table, and optional WHERE clause
    private static final Pattern SQL_PATTERN = Pattern.compile(
        "SELECT\\s+(.+?)\\s+FROM\\s+(\\w+)(?:\\s+WHERE\\s+(.+))?",
        Pattern.CASE_INSENSITIVE
    );

    // Regex to parse the WHERE condition (e.g., age > 30)
    private static final Pattern WHERE_PATTERN = Pattern.compile(
        "(\\w+)\\s*([><=])\\s*(\\S+)",
        Pattern.CASE_INSENSITIVE
    );

    public static Query parse(String sql) {
        Matcher matcher = SQL_PATTERN.matcher(sql.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid SQL query format.");
        }

        String columnsPart = matcher.group(1);
        String tableName = matcher.group(2);
        String wherePart = matcher.group(3);

        List<String> columns = Arrays.stream(columnsPart.split(","))
                                     .map(String::trim)
                                     .collect(Collectors.toList());

        FilterCondition condition = null;
        if (wherePart != null) {
            Matcher whereMatcher = WHERE_PATTERN.matcher(wherePart.trim());
            if (!whereMatcher.matches()) {
                throw new IllegalArgumentException("Invalid WHERE clause format.");
            }
            String column = whereMatcher.group(1);
            String operator = whereMatcher.group(2);
            String valueStr = whereMatcher.group(3);

            // Attempt to parse value as an Integer, otherwise treat as String
            Object value;
            try {
                value = Integer.parseInt(valueStr);
            } catch (NumberFormatException e) {
                value = valueStr.replaceAll("['\"]", ""); // Remove quotes
            }
            condition = new FilterCondition(column, operator, value);
        }

        return new Query(columns, tableName, condition);
    }
}
