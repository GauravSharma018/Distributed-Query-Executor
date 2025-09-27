# Mini-SQL: A Distributed Query Executor in Java

This project is a simplified simulation of a distributed SQL query executor, built entirely in Java. It demonstrates how a complex query can be broken down, processed in parallel across multiple worker nodes, and aggregated to produce a final result. The primary goal is to showcase the principles of distributed computing and parallel data processing to achieve significant performance gains over traditional, single-threaded execution.



## The Problem: Why Distributed Processing?

In the age of "Big Data," applications often deal with datasets containing millions or even billions of records. A single computer (a monolithic system) quickly hits fundamental limits when trying to process such vast amounts of data:
* **Memory Bottleneck**: The entire dataset may not fit into the RAM of a single machine.
* **CPU Bottleneck**: A single CPU can only perform one operation at a time, making sequential scans incredibly slow.
* **I/O Bottleneck**: Reading massive files from a single disk is a slow process.

**The solution is a distributed system.** Instead of relying on one powerful machine, we use a cluster of simpler machines (or "nodes") that work together. This project simulates that environment to solve the problem by applying a "divide and conquer" strategy to data processing.

---
## ✨ Features

- **Distributed Execution**: Queries are parallelized across multiple worker nodes, each handling a partition of the total dataset.
- **SQL Support**: Supports basic `SELECT` and `FILTER` (`WHERE`) operations.
    - `SELECT col1, col2 FROM ...`
    - `SELECT * FROM ...`
    - `... WHERE column > value`
    - `... WHERE column = value`
- **Query Parsing**: A regex-based parser deconstructs SQL queries into a structured, executable format.
- **Basic Indexing**: Each worker can build an index on a column to dramatically speed up lookups, simulating database indexing.
- **Benchmarking**: The application directly compares the performance of distributed vs. sequential execution, showing a clear speedup factor.

---
## ⚙️ How It Works

The architecture consists of two main components: a **Coordinator** and multiple **Workers**.

1.  **Data Partitioning**: A large dataset is generated and split into several smaller partitions.
2.  **Worker Initialization**: Each worker node is initialized with one data partition and builds a local index on a specified column (e.g., `id`).
3.  **Query Execution**:
    - The **Coordinator** receives a raw SQL query string.
    - It uses the `QueryParser` to convert the string into a structured `Query` object.
    - The Coordinator creates a task for each **Worker** and submits them to a thread pool to be executed in parallel.
    - Each **Worker** executes the sub-query on its local data partition, using its index if possible to find relevant rows quickly.
    - The Coordinator collects the results from all workers and aggregates them into a single final list.
---

## Core Concepts Explained

This project is a practical demonstration of several key computer science concepts that are fundamental to modern data engineering and database design.

#### 1. Data Partitioning
This is the process of breaking a large dataset into smaller, manageable chunks called **partitions**. Each worker node in our system is responsible for only one partition. This is the "divide" part of "divide and conquer" and is the first step in enabling parallel processing.

#### 2. Parallel Execution
Once the data is partitioned, multiple worker nodes can perform the same operation (like filtering data) on their respective partitions **at the same time**. Instead of one person reading an entire 1000-page book, we give 10 people each a 100-page section to read simultaneously. This parallelism drastically reduces the total time required to complete the task. This is achieved in the project using a Java `ExecutorService` (a thread pool).

#### 3. Query Parsing and Execution Planning
A computer doesn't understand a raw SQL string like `"SELECT id, name FROM users WHERE age > 30"`. **Parsing** is the process of breaking this string down into a structured object that the program can understand (e.g., columns to select, the table to query, and the conditions for filtering). The **Execution Plan** is the strategy for executing this query. In our system, the plan is simple: send the parsed query to all worker nodes to execute on their partition.

#### 4. Indexing
Searching for a specific record in a large, unsorted dataset requires a **full scan**—looking at every single row until a match is found. This is very inefficient. An **index** is a special data structure (like the index at the back of a book) that maps data values to their locations. When we query for `id = 500000`, the index allows the worker to jump directly to that row without scanning everything else, providing a massive performance boost for lookups.

---
## System Architecture and Query Lifecycle

The system is designed around a **Coordinator-Worker** (or Master-Slave) architecture, a common pattern in distributed computing.

### Components
* **The Coordinator (`Coordinator.java`)**: The "brain" of the operation. It doesn't store any data itself. Its responsibilities are:
    1.  Receive the user's SQL query.
    2.  Parse the query into a machine-readable format.
    3.  Create an execution plan and distribute tasks to all worker nodes.
    4.  Wait for all workers to finish and collect their individual results.
    5.  Aggregate the results into a single, final list to return to the user.

* **The Worker (`Worker.java`)**: The "muscle" of the system. We simulate multiple workers, each with the following responsibilities:
    1.  Hold a unique partition of the total dataset.
    2.  Maintain a local index on its partition to speed up searches.
    3.  Execute the sub-query task assigned by the Coordinator.
    4.  Return its local results back to the Coordinator.

### The Lifecycle of a Query
1.  **Submission**: A user submits a query to the `Main` class.
2.  **Parsing**: The `Coordinator` receives the query and uses the `QueryParser` to break it into a structured `Query` object.
3.  **Distribution**: The `Coordinator` creates a task for each `Worker` and submits them to a Java `ExecutorService`, which runs each task on a separate thread in parallel.
4.  **Execution**: Each `Worker` executes the query on its local data partition. It first checks its **index** for the `WHERE` clause column. If a relevant index exists, it finds the matching rows almost instantly. If not, it performs a full scan of its (much smaller) partition.
5.  **Projection**: The `Worker` filters the rows and then "projects" them, meaning it creates new rows containing only the columns requested in the `SELECT` statement.
6.  **Aggregation**: The `Coordinator` collects the lists of results from each `Worker` as they finish and merges them into one final result set.
7.  **Completion**: The final, aggregated result is returned.

---
## Project Structure and Rationale

The project is organized into packages to follow the principle of **Separation of Concerns**, which makes the code modular, easier to understand, and maintain.

```
src/
└── com/
    └── minisql/
        ├── Main.java              # Entry point, simulation driver, and benchmark runner.
        ├── Coordinator.java       # The master node logic.
        ├── Worker.java            # The worker node logic.
        ├── model/                 # Data Structures (POJOs). Separates data from logic.
        │   ├── FilterCondition.java
        │   ├── Query.java
        │   └── Row.java
        └── parser/                # Query Parsing Logic. Isolated for maintainability.
            └── QueryParser.java
```

* **`model` package**: Contains simple Java objects that represent data (`Row`) or parts of the query (`Query`, `FilterCondition`). Keeping these separate from the execution logic is a core principle of good software design.
* **`parser` package**: All the logic for understanding the SQL language is isolated here. This means we could swap it out for a more advanced parser (like one using ANTLR) without changing the rest of the engine.
* **Root package**: Contains the main architectural components (`Coordinator`, `Worker`) and the application entry point (`Main`).

---
## How to Run the Project

The project is built with standard Java and has no external dependencies. You only need a Java Development Kit (JDK) installed (version 8 or later).

1.  **Clone the Repository**:
    ```bash
    git clone https://github.com/GauravSharma018/Distributed-Query-Executor
    cd mini-sql-engine
    ```

2.  **Compile the Java Files**:
    From the **root directory** of the project (`mini-sql-engine/`), run the following command to compile all source files into the `src` directory itself:
    ```bash
    javac -d src src/com/minisql/*.java src/com/minisql/model/*.java src/com/minisql/parser/*.java
    ```

3.  **Run the Simulation**:
    Now, from the **root directory**, run the `Main` class using its fully qualified name. The `-cp src` flag tells Java to look for compiled classes in the `src` folder.
    ```bash
    java -cp src com.minisql.Main
    ```

### Expected Output
You should see output that first sets up the simulation, then runs the benchmarks, and finally prints a summary of the performance gains.
```
Generating 1000000 rows of data...
Data generation complete.
Worker 0 initialized with 250000 rows.
Worker 0 created index on column 'id'.
... (other workers initialize) ...

Executing query: SELECT id, name, age FROM users WHERE id = 500000

--- Running Sequentially (Single Thread) ---
Sequential execution took: 68 ms
Results found: 1
{id=500000, name=User_500000, age=...}

--- Running Distributed (4 Workers in Parallel) ---
Distributed execution took: 25 ms
Results found: 1
{id=500000, name=User_500000, age=...}

--- Benchmark Summary ---
Sequential Time: 68 ms
Distributed Time: 25 ms
Speedup: 2.72x
```
## 📄 License

This project is open-source and available under the **MIT License**. See the `LICENSE` file for more details.
