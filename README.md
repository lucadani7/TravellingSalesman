# TravellingSalesman

A Java implementation exploring solutions to the classic **Travelling Salesman Problem (TSP)** — given a set of cities and the distances between them, find the shortest possible route that visits every city exactly once and returns to the starting point.


## Tech Stack

- **Language:** Java 21
- **Build tool:** Maven


## Dependencies

| Library | Purpose |
|---|---|
| [JGraphT (guava module)](https://mvnrepository.com/artifact/org.jgrapht/jgrapht-guava) | Graph data structures and algorithms used to model cities/routes as a graph |
| [JFreeChart](https://www.jfree.org/jfreechart/) | Charting and visualization of results (e.g. route/cost plots) |
| [Lombok](https://projectlombok.org/) | Reduces boilerplate (getters/setters, constructors, etc.) |
| [JUnit Jupiter](https://junit.org/junit5/) | Unit testing |


## Prerequisites

- JDK 21 or newer
- Maven 3.6+


## Getting Started

1. Clone the repository:

   ```bash
   git clone https://github.com/lucadani7/TravellingSalesman.git
   cd TravellingSalesman
   ```

2. Build the project with Maven:

   ```bash
   mvn clean install
   ```

3. Run the test suite:

   ```bash
   mvn test
   ```


## Contributing

Contributions, issues, and feature requests are welcome. Feel free to open a pull request or file an issue.


## License

This project is licensed under the **Apache License 2.0** — see the [LICENSE](./LICENSE) file for details.
