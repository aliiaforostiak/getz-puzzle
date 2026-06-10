package org.example.rest.engine;

import org.example.puzzle.BoardPosition;
import org.example.puzzle.ConcurrentPuzzleSolver;
import org.example.puzzle.Direction;
import org.example.puzzle.SlidingPuzzle;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

@Service
public class ConcurrentPuzzleSolvingEngine implements PuzzleSolvingEngine {

    @Override
    public List<Direction> solve(BoardPosition startPosition, long timeoutSeconds) throws InterruptedException, TimeoutException {
        ExecutorService executor = Executors.newFixedThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors()));
        try {
            ConcurrentPuzzleSolver<BoardPosition, Direction> solver =
                    new ConcurrentPuzzleSolver<>(new SlidingPuzzle(startPosition), executor);
            return solver.solve(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }
    }
}
