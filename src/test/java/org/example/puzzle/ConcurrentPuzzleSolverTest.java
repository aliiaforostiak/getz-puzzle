package org.example.puzzle;

import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConcurrentPuzzleSolverTest {

    @Test
    void solvesAlreadySolvedPuzzleWithEmptyPath() throws InterruptedException {
        BoardPosition start = new BoardPosition(3, List.of(
                1, 2, 3,
                4, 5, 6,
                7, 8, 0
        ));

        SlidingPuzzle puzzle = new SlidingPuzzle(start);
        ExecutorService executor = new QueueExecutorService();
        try {
            ConcurrentPuzzleSolver<BoardPosition, Direction> solver = new ConcurrentPuzzleSolver<>(puzzle, executor);

            List<Direction> solution = solver.solve();

            assertEquals(List.of(), solution);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void solvesMultiStepPuzzleAndRestoresCompletePath() throws InterruptedException {
        GraphPuzzle puzzle = new GraphPuzzle(
                "start",
                "goal",
                Map.of(
                        "start", List.of("toB"),
                        "b", List.of("toC"),
                        "c", List.of("toGoal"),
                        "goal", List.of()
                ),
                Map.of(
                        new Transition("start", "toB"), "b",
                        new Transition("b", "toC"), "c",
                        new Transition("c", "toGoal"), "goal"
                )
        );

        ExecutorService executor = new QueueExecutorService();
        try {
            ConcurrentPuzzleSolver<String, String> solver = new ConcurrentPuzzleSolver<>(puzzle, executor);

            List<String> solution = solver.solve();

            assertEquals(List.of("toB", "toC", "toGoal"), solution);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void ignoresDuplicateStatesReachedByDifferentBranches() throws InterruptedException {
        AtomicInteger expandedDuplicateStateCount = new AtomicInteger();
        GraphPuzzle puzzle = new GraphPuzzle(
                "start",
                "goal",
                Map.of(
                        "start", List.of("toB", "toC"),
                        "b", List.of("toDuplicate"),
                        "c", List.of("toDuplicate"),
                        "duplicate", List.of("toGoal"),
                        "goal", List.of()
                ),
                Map.of(
                        new Transition("start", "toB"), "b",
                        new Transition("start", "toC"), "c",
                        new Transition("b", "toDuplicate"), "duplicate",
                        new Transition("c", "toDuplicate"), "duplicate",
                        new Transition("duplicate", "toGoal"), "goal"
                ),
                expandedDuplicateStateCount
        );

        ExecutorService executor = new QueueExecutorService();
        try {
            ConcurrentPuzzleSolver<String, String> solver = new ConcurrentPuzzleSolver<>(puzzle, executor);

            List<String> solution = solver.solve();

            assertEquals(List.of("toB", "toDuplicate", "toGoal"), solution);
            assertEquals(1, expandedDuplicateStateCount.get());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void returnsShortestFoundPathForSimplePuzzle() throws InterruptedException {
        BoardPosition start = new BoardPosition(3, List.of(
                1, 2, 3,
                4, 5, 6,
                0, 7, 8
        ));

        SlidingPuzzle puzzle = new SlidingPuzzle(start);
        ExecutorService executor = new QueueExecutorService();
        try {
            ConcurrentPuzzleSolver<BoardPosition, Direction> solver = new ConcurrentPuzzleSolver<>(puzzle, executor);

            List<Direction> solution = solver.solve();

            assertEquals(List.of(Direction.RIGHT, Direction.RIGHT), solution);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void throwsTimeoutWhenNoSolutionIsFoundWithinLimit() {
        GraphPuzzle puzzle = new GraphPuzzle(
                "start",
                "goal",
                Map.of(
                        "start", List.of("toB"),
                        "b", List.of("toStart")
                ),
                Map.of(
                        new Transition("start", "toB"), "b",
                        new Transition("b", "toStart"), "start"
                )
        );

        QueueExecutorService executor = new QueueExecutorService();
        try {
            ConcurrentPuzzleSolver<String, String> solver = new ConcurrentPuzzleSolver<>(puzzle, executor);

            TimeoutException exception = assertThrows(TimeoutException.class, () -> solver.solve(50, TimeUnit.MILLISECONDS));

            assertTrue(exception.getMessage().contains("Timed out"));
            assertTrue(executor.isShutdown());
        } finally {
            executor.shutdownNow();
        }
    }

    private static final class QueueExecutorService extends AbstractExecutorService {
        private final Deque<Runnable> tasks = new ArrayDeque<>();
        private boolean running;
        private boolean shutdown;

        @Override
        public void shutdown() {
            shutdown = true;
        }

        @Override
        public List<Runnable> shutdownNow() {
            shutdown = true;
            List<Runnable> remaining = List.copyOf(tasks);
            tasks.clear();
            return remaining;
        }

        @Override
        public boolean isShutdown() {
            return shutdown;
        }

        @Override
        public boolean isTerminated() {
            return shutdown && tasks.isEmpty() && !running;
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) {
            return true;
        }

        @Override
        public void execute(Runnable command) {
            if (shutdown) {
                throw new IllegalStateException("executor is shut down");
            }
            tasks.addLast(command);
            if (running) {
                return;
            }

            running = true;
            try {
                while (!tasks.isEmpty()) {
                    tasks.removeFirst().run();
                }
            } finally {
                running = false;
            }
        }
    }

    private record Transition(String position, String move) {
        private Transition {
            Objects.requireNonNull(position);
            Objects.requireNonNull(move);
        }
    }

    private static final class GraphPuzzle implements Puzzle<String, String> {
        private final String initialPosition;
        private final String goalPosition;
        private final Map<String, List<String>> legalMovesByPosition;
        private final Map<Transition, String> nextPositionByTransition;
        private final AtomicInteger duplicateExpansionCounter;

        private GraphPuzzle(String initialPosition,
                            String goalPosition,
                            Map<String, List<String>> legalMovesByPosition,
                            Map<Transition, String> nextPositionByTransition) {
            this(initialPosition, goalPosition, legalMovesByPosition, nextPositionByTransition, new AtomicInteger());
        }

        private GraphPuzzle(String initialPosition,
                            String goalPosition,
                            Map<String, List<String>> legalMovesByPosition,
                            Map<Transition, String> nextPositionByTransition,
                            AtomicInteger duplicateExpansionCounter) {
            this.initialPosition = initialPosition;
            this.goalPosition = goalPosition;
            this.legalMovesByPosition = legalMovesByPosition;
            this.nextPositionByTransition = nextPositionByTransition;
            this.duplicateExpansionCounter = duplicateExpansionCounter;
        }

        @Override
        public String initialPosition() {
            return initialPosition;
        }

        @Override
        public boolean isGoal(String position) {
            return goalPosition.equals(position);
        }

        @Override
        public List<String> legalMoves(String position) {
            if ("duplicate".equals(position)) {
                duplicateExpansionCounter.incrementAndGet();
            }
            return legalMovesByPosition.getOrDefault(position, List.of());
        }

        @Override
        public String move(String position, String move) {
            String nextPosition = nextPositionByTransition.get(new Transition(position, move));
            if (nextPosition == null) {
                throw new IllegalArgumentException("No transition for " + position + " + " + move);
            }
            return nextPosition;
        }
    }
}
