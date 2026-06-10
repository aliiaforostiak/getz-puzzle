# Concurrent Puzzle Solver

Spring Boot REST service for solving sliding puzzles using the concurrent search approach from *Java Concurrency in Practice*.

## What it does

- Solves an N x N sliding puzzle.
- Runs the search concurrently with `ExecutorService`.
- Stops duplicate state expansion.
- Returns the solution path, board states, and execution time.
- Exposes a REST API with Swagger/OpenAPI documentation.

## Tech Stack

- Java 21
- Spring Boot 3.5.3
- Maven
- JUnit 5
- springdoc-openapi

## Run

Run tests:

```bash
mvn test
```

Run the application:

```bash
mvn spring-boot:run
```

Build without tests:

```bash
mvn compile
```

Open the app in a browser:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## REST API

Base path: `/api/puzzles`

### `POST /api/puzzles/solve`

Solves a puzzle.

Request body:

```json
{
  "size": 3,
  "tiles": [1, 2, 3, 4, 5, 6, 0, 7, 8],
  "timeoutSeconds": 15
}
```

Successful response `200 OK`:

```json
{
  "solved": true,
  "moves": ["RIGHT", "RIGHT"],
  "boards": [
    [1, 2, 3, 4, 5, 6, 0, 7, 8],
    [1, 2, 3, 4, 5, 6, 7, 0, 8],
    [1, 2, 3, 4, 5, 6, 7, 8, 0]
  ],
  "durationMs": 12,
  "message": "Solved"
}
```

Possible errors:

- `400 Bad Request` - invalid JSON, wrong board size, duplicate tiles, missing `0`, `timeoutSeconds <= 0`.
- `408 Request Timeout` - no solution was found within the given time.
- `500 Internal Server Error` - unexpected server failure.

Error response example:

```json
{
  "status": 408,
  "error": "Request Timeout",
  "message": "No solution found within 15 seconds",
  "timestamp": "2026-06-10T08:00:00Z",
  "details": []
}
```

### `GET /api/puzzles/health`

Returns application health.

```json
{
  "status": "UP"
}
```

## Project Structure

- `src/main/java/org/example/PuzzleRestApplication.java` - Spring Boot entrypoint.
- `src/main/java/org/example/puzzle/ConcurrentPuzzleSolver.java` - concurrent search implementation.
- `src/main/java/org/example/puzzle/SlidingPuzzle.java` - 8-puzzle rules.
- `src/main/java/org/example/puzzle/BoardPosition.java` - immutable board state.
- `src/main/java/org/example/puzzle/Direction.java` - possible moves.
- `src/main/java/org/example/puzzle/Puzzle.java` - puzzle contract.
- `src/main/java/org/example/puzzle/PuzzleNode.java` - search tree node.
- `src/main/java/org/example/puzzle/ValueLatch.java` - one-time result latch.
- `src/main/java/org/example/rest/controller/*` - REST controller and exception handling.
- `src/main/java/org/example/rest/service/*` - service layer.
- `src/main/java/org/example/rest/dto/*` - request/response DTOs.
- `src/main/java/org/example/rest/engine/*` - adapter around the solver.

## Tests

Tests cover:

- `BoardPosition` validation and immutability.
- `SlidingPuzzle` move rules.
- `ValueLatch` waiting and timeout behavior.
- `ConcurrentPuzzleSolver` happy path, duplicates, timeout, and shutdown race.
- REST controller, full integration flow, and OpenAPI documentation.

Run a specific test class:

```bash
mvn -Dtest=PuzzleControllerTest test
```

## Documentation

- `docs/concurrent-puzzle-solver.html` - analyst overview.
- `docs/developer-guide.html` - developer guide.

## Repository

- `https://github.com/aliiaforostiak/getz-puzzle`

## Notes

- The application is REST-first now.
- The old console `Main` entrypoint was removed.
- If you want a new demo launcher, it should be added as a separate sample, not as the primary entrypoint.
