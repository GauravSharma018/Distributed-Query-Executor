package com.minisql;

import com.minisql.model.Query;
import com.minisql.model.Row;
import com.minisql.parser.QueryParser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

// The coordinator (or master) node that receives queries and distributes work.
public class Coordinator {
    private final List<Worker> workers;
    private final ExecutorService executorService;

    public Coordinator(List<Worker> workers) {
        this.workers = workers;
        // Create a thread pool with one thread per worker
        this.executorService = Executors.newFixedThreadPool(workers.size());
    }

    // Executes a query in a distributed fashion across all workers.
    public List<Row> executeDistributedQuery(String sql) {
        Query query = QueryParser.parse(sql);

        List<Callable<List<Row>>> tasks = new ArrayList<>();
        for (Worker worker : workers) {
            tasks.add(() -> worker.executeSubQuery(query));
        }

        List<Row> aggregatedResults = new ArrayList<>();
        try {
            // Submit all tasks and wait for them to complete
            List<Future<List<Row>>> futures = executorService.invokeAll(tasks);
            for (Future<List<Row>> future : futures) {
                aggregatedResults.addAll(future.get());
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }

        return aggregatedResults;
    }

    // Shuts down the thread pool.
    public void shutdown() {
        executorService.shutdown();
    }
}
