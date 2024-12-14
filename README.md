# ZooKeeper Use Cases Project

This project demonstrates various use cases of Apache ZooKeeper with practical examples, implemented as Java tests. It showcases configuration management, node creation, value updates, and event watching with ZooKeeper.

## Prerequisites

- **Java 17** or later
- **Maven** (for building and running tests)
- **Docker** and **Docker Compose** (for setting up the ZooKeeper cluster)

## Project Overview

This project includes:

1. **Node Creation**: Dynamically create ZooKeeper nodes with data.
2. **Node Deletion**: Recursively delete ZooKeeper nodes.
3. **Node Updates**: Update node values programmatically.
4. **Event Watching**: Establish watches on nodes to respond to changes.
5. **Unit Tests**: JUnit-based tests to validate ZooKeeper operations.

## Running the Project

Follow these steps to set up ZooKeeper and run the tests.

### 1. Start the ZooKeeper Cluster

Use Docker Compose to start a ZooKeeper cluster:

```bash
docker-compose up -d
```

This command starts the ZooKeeper containers in detached mode. Ensure that Docker is running on your system.

### 2. Run the Tests

Once the ZooKeeper cluster is up and running, execute the Maven tests:

```bash
mvn test
```

This command compiles the code, runs the tests, and outputs the results.

## Cleanup

To stop the ZooKeeper cluster after testing, run:

```bash
docker-compose down
```

## Structure

- **`src/main/java`**: Contains Java source code for ZooKeeper operations.
- **`src/test/java`**: Contains JUnit test cases to demonstrate ZooKeeper use cases.
- **`docker-compose.yml`**: Configuration file to set up the ZooKeeper cluster using Docker Compose.

## License

This project is open-source and available under the [MIT License](LICENSE).

