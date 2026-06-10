package org.example.puzzle;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class ConcurrentPuzzleSolver<P, M> {
    private final Puzzle<P, M> puzzle;
    private final ExecutorService executor;

    public ConcurrentPuzzleSolver(Puzzle<P, M> puzzle, ExecutorService executor) {
        this.puzzle = puzzle;
        this.executor = executor;
    }

    public List<M> solve() throws InterruptedException {
        try {
            return solve(15, TimeUnit.SECONDS);
        } catch (TimeoutException exception) {
            throw new IllegalStateException("No solution found within 15 seconds", exception);
        }
    }

    public List<M> solve(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        ValueLatch<PuzzleNode<P, M>> solution = new ValueLatch<>();
        ConcurrentMap<P, Boolean> seen = new ConcurrentHashMap<>();

        executor.execute(newTask(new PuzzleNode<>(null, null, puzzle.initialPosition()), solution, seen));

        try {
            PuzzleNode<P, M> solvedNode = solution.getValue(timeout, unit);
            return solvedNode.asMoveList();
        } catch (TimeoutException exception) {
            executor.shutdownNow();
            throw exception;
        }
    }

    private Runnable newTask(PuzzleNode<P, M> node,
                             ValueLatch<PuzzleNode<P, M>> solution,
                             ConcurrentMap<P, Boolean> seen) {
        return () -> {
            if (solution.isSet()) {
                return;
            }
            if (seen.putIfAbsent(node.position(), Boolean.TRUE) != null) {
                return;
            }
            if (puzzle.isGoal(node.position())) {
                solution.setValue(node);
                return;
            }
            for (M move : puzzle.legalMoves(node.position())) {
                P nextPosition = puzzle.move(node.position(), move);
                try {
                    executor.execute(newTask(new PuzzleNode<>(node, move, nextPosition), solution, seen));
                } catch (RejectedExecutionException ignored) {
                    return;
                }
            }
        };
    }
}
