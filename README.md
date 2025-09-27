# Mini-SQL: A Distributed Query Executor in Java

This project is a simplified simulation of a distributed SQL query executor, built entirely in Java. It demonstrates how a complex query can be broken down, processed in parallel across multiple worker nodes, and aggregated to produce a final result, achieving significant speedup compared to traditional sequential execution.

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
## 🚀 How to Run

The project is built with standard Java and has no external dependencies.

1.  **Clone the repository**:
    ```bash
    git clone [https://github.com/your-username/mini-sql-engine.git](https://github.com/your-username/mini-sql-engine.git)
    cd mini-sql-engine
    ```

2.  **Compile the code**:
    Navigate to the `src` directory and compile all `.java` files.
    ```bash
    cd src
    javac com/minisql/Main.java com/minisql/*.java com/minisql/model/*.java com/minisql/parser/*.java
    ```

3.  **Run the application**:
    From the `src` directory, run the `Main` class.
    ```bash
    java com.minisql.Main
    ```

---
### Example Output

Running the `Main` class will produce output similar to this, demonstrating the performance difference:
