package com.minisql;

import com.minisql.model.Row;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {
    private static final int TOTAL_ROWS = 1_000_000;
    private static final int NUM_WORKERS = 4;

    public static void main(String[] args) {
        // 1. Generate a large dataset
        System.out.println("Generating " + TOTAL_ROWS + " rows of data...");
        List<Row> dataset = generateData();
        System.out.println("Data generation complete.");

        // 2. Partition the data for the workers
        List<List<Row>> partitions = partitionData(dataset, NUM_WORKERS);

        // 3. Create and initialize worker nodes
        List<Worker> workers = new ArrayList<>();
        for (int i = 0; i < NUM_WORKERS; i++) {
            Worker worker = new Worker(i, partitions.get(i));
            worker.createIndex("id"); // Create an index on the 'id' column
            workers.add(worker);
        }

        // 4. Define the SQL query
        String sqlQuery = "SELECT id, name, age FROM users WHERE id = 500000";
        System.out.println("\nExecuting query: " + sqlQuery);

        // 5. Execute sequentially for baseline benchmark
        System.out.println("\n--- Running Sequentially (Single Thread) ---");
        Instant startSequential = Instant.now();
        List<Row> sequentialResults = executeSequentially(dataset, sqlQuery);
        Instant endSequential = Instant.now();
        long sequentialTime = Duration.between(startSequential, endSequential).toMillis();
        System.out.println("Sequential execution took: " + sequentialTime + " ms");
        System.out.println("Results found: " + sequentialResults.size());
        sequentialResults.forEach(System.out::println);


        // 6. Execute in a distributed fashion
        System.out.println("\n--- Running Distributed (" + NUM_WORKERS + " Workers in Parallel) ---");
        Coordinator coordinator = new Coordinator(workers);
        Instant startDistributed = Instant.now();
        List<Row> distributedResults = coordinator.executeDistributedQuery(sqlQuery);
        Instant endDistributed = Instant.now();
        long distributedTime = Duration.between(startDistributed, endDistributed).toMillis();
        coordinator.shutdown();

        System.out.println("Distributed execution took: " + distributedTime + " ms");
        System.out.println("Results found: " + distributedResults.size());
        distributedResults.forEach(System.out::println);

        // 7. Compare results
        System.out.println("\n--- Benchmark Summary ---");
        System.out.printf("Sequential Time: %d ms%n", sequentialTime);
        System.out.printf("Distributed Time: %d ms%n", distributedTime);
        if (distributedTime > 0) {
            double speedup = (double) sequentialTime / distributedTime;
            System.out.printf("Speedup: %.2fx%n", speedup);
        }
    }

    // Simulates sequential execution on the entire dataset
    private static List<Row> executeSequentially(List<Row> dataset, String sql) {
        // For a fair comparison, simulate the same work as one worker but on all data
        Worker singleNode = new Worker(99, dataset);
        singleNode.createIndex("id");
        return singleNode.executeSubQuery(com.minisql.parser.QueryParser.parse(sql));
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
