package com.minisql;

import com.minisql.model.Row;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Main {
    private static final int TOTAL_ROWS = 1_000_000;
    private static final int NUM_WORKERS = 4;

    public static void main(String[] args) {
        // 1. Generate a large dataset
        System.out.println("Generating " + TOTAL_ROWS + " rows of data...");
        List<Row> dataset = generateData();
        System.out.println("Data generation complete.");
        System.out.println("Dataset contains columns: id, name, age, department");

        // 2. Partition the data for the workers
        List<List<Row>> partitions = partitionData(dataset, NUM_WORKERS);

        // 3. Create and initialize worker nodes
        List<Worker> workers = new ArrayList<>();
        for (int i = 0; i < NUM_WORKERS; i++) {
            Worker worker = new Worker(i, partitions.get(i));
            // Create an index on common query columns for better performance
            worker.createIndex("id");
            worker.createIndex("age");
            workers.add(worker);
        }

        // 4. Setup the Coordinator
        Coordinator coordinator = new Coordinator(workers);
        
        // 5. Start the interactive query loop using try-with-resources for the Scanner
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("\nWelcome to the Mini-SQL Engine!");
            System.out.println("Type a query (e.g., SELECT * FROM users WHERE age > 55) or 'exit' to quit.");

            while (true) {
                System.out.print("\nmini-sql> ");
                String sqlQuery = scanner.nextLine();

                // Check for exit command
                if (sqlQuery.trim().equalsIgnoreCase("exit") || sqlQuery.trim().equalsIgnoreCase("quit")) {
                    System.out.println("Exiting Mini-SQL engine. Goodbye!");
                    break; // Exit the loop
                }
                
                // Check for empty input
                if (sqlQuery.trim().isEmpty()) {
                    continue;
                }

                try {
                    // Execute the user's query in a distributed fashion
                    System.out.println("--- Running Distributed (" + NUM_WORKERS + " Workers in Parallel) ---");
                    Instant startDistributed = Instant.now();
                    List<Row> distributedResults = coordinator.executeDistributedQuery(sqlQuery);
                    Instant endDistributed = Instant.now();
                    long distributedTime = Duration.between(startDistributed, endDistributed).toMillis();

                    System.out.println("Distributed execution took: " + distributedTime + " ms");
                    System.out.println("Results found: " + distributedResults.size());

                    // Print the first 10 results to avoid flooding the console
                    int limit = Math.min(distributedResults.size(), 10);
                    for (int i = 0; i < limit; i++) {
                        System.out.println(distributedResults.get(i));
                    }
                    if (distributedResults.size() > 10) {
                        System.out.println("... (" + (distributedResults.size() - 10) + " more rows)");
                    }

                } catch (IllegalArgumentException e) {
                    // Catch parsing or syntax errors and allow the user to try again
                    System.err.println("Syntax Error: " + e.getMessage());
                }
            }
        } // The Scanner is automatically closed here by the try-with-resources block

        // 6. Cleanup coordinator's thread pool
        coordinator.shutdown();
    }

    // Generates a sample dataset
    private static List<Row> generateData() {
        List<Row> data = new ArrayList<>(TOTAL_ROWS);
        Random random = new Random();
        String[] departments = {"Engineering", "HR", "Sales", "Marketing"};
        for (int i = 0; i < TOTAL_ROWS; i++) {
            Row row = new Row();
            row.addValue("id", i);
            row.addValue("name", "User_" + i);
            row.addValue("age", 20 + random.nextInt(40)); // Ages 20-59
            row.addValue("department", departments[random.nextInt(departments.length)]);
            data.add(row);
        }
        return data;
    }

    // Splits the dataset into N partitions
    private static List<List<Row>> partitionData(List<Row> dataset, int numPartitions) {
        List<List<Row>> partitions = new ArrayList<>(numPartitions);
        int partitionSize = dataset.size() / numPartitions;
        int remainder = dataset.size() % numPartitions;

        int offset = 0;
        for (int i = 0; i < numPartitions; i++) {
            int size = partitionSize + (i < remainder ? 1 : 0);
            partitions.add(new ArrayList<>(dataset.subList(offset, offset + size)));
            offset += size;
        }
        return partitions;
    }
}
