package org.example.rest.service;

import org.example.puzzle.BoardPosition;
import org.example.puzzle.Direction;
import org.example.rest.controller.PuzzleSolveTimeoutException;
import org.example.rest.dto.SolvePuzzleRequest;
import org.example.rest.dto.SolvePuzzleResponse;
import org.example.rest.engine.PuzzleSolvingEngine;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PuzzleSolverServiceTest {

    @Test
    void solveMapsSuccessfulResultAndBuildsBoardStates() {
        CapturingEngine engine = new CapturingEngine(List.of(Direction.RIGHT, Direction.RIGHT));
        PuzzleSolverService service = new PuzzleSolverService(engine);
        SolvePuzzleRequest request = new SolvePuzzleRequest(3, List.of(
                1, 2, 3,
                4, 5, 6,
                0, 7, 8
        ), null);

        SolvePuzzleResponse response = service.solve(request);

        assertTrue(response.solved());
        assertEquals(List.of("RIGHT", "RIGHT"), response.moves());
        assertEquals(List.of(
                List.of(1, 2, 3, 4, 5, 6, 0, 7, 8),
                List.of(1, 2, 3, 4, 5, 6, 7, 0, 8),
                List.of(1, 2, 3, 4, 5, 6, 7, 8, 0)
        ), response.boards());
        assertEquals("Solved", response.message());
        assertEquals(15L, engine.capturedTimeoutSeconds);
    }

    @Test
    void solveUsesExplicitTimeoutWhenProvided() {
        CapturingEngine engine = new CapturingEngine(List.of());
        PuzzleSolverService service = new PuzzleSolverService(engine);
        SolvePuzzleRequest request = new SolvePuzzleRequest(3, List.of(
                1, 2, 3,
                4, 5, 6,
                0, 7, 8
        ), 3L);

        service.solve(request);

        assertEquals(3L, engine.capturedTimeoutSeconds);
    }

    @Test
    void solveConvertsTimeoutToUnsolvedResponse() {
        PuzzleSolvingEngine engine = new PuzzleSolvingEngine() {
            @Override
            public List<Direction> solve(BoardPosition startPosition, long timeoutSeconds) throws InterruptedException, TimeoutException {
                throw new TimeoutException("timed out");
            }
        };
        PuzzleSolverService service = new PuzzleSolverService(engine);
        SolvePuzzleRequest request = new SolvePuzzleRequest(3, List.of(
                1, 2, 3,
                4, 5, 6,
                0, 7, 8
        ), 1L);

        PuzzleSolveTimeoutException exception = assertThrows(PuzzleSolveTimeoutException.class, () -> service.solve(request));

        assertEquals(1L, exception.timeoutSeconds());
    }

    @Test
    void solveRejectsInvalidTileConfiguration() {
        CapturingEngine engine = new CapturingEngine(List.of());
        PuzzleSolverService service = new PuzzleSolverService(engine);
        SolvePuzzleRequest invalidRequest = new SolvePuzzleRequest(3, List.of(
                1, 2, 3,
                4, 5, 6,
                7, 7, 8
        ), 1L);

        assertThrows(IllegalArgumentException.class, () -> service.solve(invalidRequest));
    }

    @Test
    void solveRejectsMissingBlankTile() {
        CapturingEngine engine = new CapturingEngine(List.of());
        PuzzleSolverService service = new PuzzleSolverService(engine);
        SolvePuzzleRequest invalidRequest = new SolvePuzzleRequest(2, List.of(1, 2, 3, 4), 1L);

        assertThrows(IllegalArgumentException.class, () -> service.solve(invalidRequest));
    }

    private static final class CapturingEngine implements PuzzleSolvingEngine {
        private final List<Direction> movesToReturn;
        private long capturedTimeoutSeconds;

        private CapturingEngine(List<Direction> movesToReturn) {
            this.movesToReturn = movesToReturn;
        }

        @Override
        public List<Direction> solve(BoardPosition startPosition, long timeoutSeconds) {
            this.capturedTimeoutSeconds = timeoutSeconds;
            return movesToReturn;
        }
    }
}
