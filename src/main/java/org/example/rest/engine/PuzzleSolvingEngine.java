package org.example.rest.engine;

import org.example.puzzle.Direction;
import org.example.puzzle.BoardPosition;

import java.util.List;
import java.util.concurrent.TimeoutException;

public interface PuzzleSolvingEngine {
    List<Direction> solve(BoardPosition startPosition, long timeoutSeconds) throws InterruptedException, TimeoutException;
}
