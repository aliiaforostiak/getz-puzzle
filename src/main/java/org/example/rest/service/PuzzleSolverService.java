package org.example.rest.service;

import org.example.puzzle.BoardPosition;
import org.example.puzzle.Direction;
import org.example.puzzle.SlidingPuzzle;
import org.example.rest.dto.SolvePuzzleRequest;
import org.example.rest.dto.SolvePuzzleResponse;
import org.example.rest.controller.PuzzleSolveTimeoutException;
import org.example.rest.engine.PuzzleSolvingEngine;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

@Service
public class PuzzleSolverService {
    private static final long DEFAULT_TIMEOUT_SECONDS = 15L;

    private final PuzzleSolvingEngine puzzleSolvingEngine;

    public PuzzleSolverService(PuzzleSolvingEngine puzzleSolvingEngine) {
        this.puzzleSolvingEngine = puzzleSolvingEngine;
    }

    public SolvePuzzleResponse solve(SolvePuzzleRequest request) {
        validate(request);

        long startedAt = System.nanoTime();
        BoardPosition startPosition = new BoardPosition(request.size(), request.tiles());
        long timeoutSeconds = request.timeoutSeconds() == null ? DEFAULT_TIMEOUT_SECONDS : request.timeoutSeconds();

        try {
            List<Direction> moves = puzzleSolvingEngine.solve(startPosition, timeoutSeconds);
            List<List<Integer>> boards = buildBoards(startPosition, moves);
            return new SolvePuzzleResponse(
                    true,
                    moves.stream().map(Direction::name).toList(),
                    boards,
                    durationMs(startedAt),
                    "Solved"
            );
        } catch (TimeoutException exception) {
            throw new PuzzleSolveTimeoutException(timeoutSeconds);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Puzzle solving was interrupted", exception);
        }
    }

    private void validate(SolvePuzzleRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }

        if (request.size() <= 0) {
            throw new IllegalArgumentException("size must be positive");
        }

        if (request.tiles() == null) {
            throw new IllegalArgumentException("tiles must not be null");
        }

        int expectedTiles;
        try {
            expectedTiles = Math.multiplyExact(request.size(), request.size());
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("size is too large", exception);
        }

        if (request.tiles().size() != expectedTiles) {
            throw new IllegalArgumentException("tile count must equal size squared");
        }

        Set<Integer> uniqueTiles = new HashSet<>(request.tiles());
        if (uniqueTiles.size() != request.tiles().size()) {
            throw new IllegalArgumentException("tiles must be unique");
        }

        for (Integer tile : request.tiles()) {
            if (tile == null) {
                throw new IllegalArgumentException("tiles must not contain null values");
            }
            if (tile < 0 || tile >= expectedTiles) {
                throw new IllegalArgumentException("tiles must be in range 0.." + (expectedTiles - 1));
            }
        }

        if (!uniqueTiles.contains(0)) {
            throw new IllegalArgumentException("tiles must contain the blank tile 0");
        }
    }

    private List<List<Integer>> buildBoards(BoardPosition startPosition, List<Direction> moves) {
        List<List<Integer>> boards = new ArrayList<>();
        boards.add(List.copyOf(startPosition.tiles()));

        SlidingPuzzle puzzle = new SlidingPuzzle(startPosition);
        BoardPosition currentPosition = startPosition;
        for (Direction move : moves) {
            currentPosition = puzzle.move(currentPosition, move);
            boards.add(List.copyOf(currentPosition.tiles()));
        }
        return boards;
    }

    private long durationMs(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000L;
    }
}
