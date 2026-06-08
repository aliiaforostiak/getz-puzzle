package org.example;

import org.example.puzzle.BoardPosition;
import org.example.puzzle.ConcurrentPuzzleSolver;
import org.example.puzzle.Direction;
import org.example.puzzle.SlidingPuzzle;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        BoardPosition start = new BoardPosition(3, List.of(
                1, 2, 3,
                4, 5, 6,
                0, 7, 8
        ));

        SlidingPuzzle puzzle = new SlidingPuzzle(start);
        ExecutorService executor = Executors.newFixedThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors()));
        try {
            ConcurrentPuzzleSolver<BoardPosition, Direction> solver = new ConcurrentPuzzleSolver<>(puzzle, executor);
            List<Direction> solution = solver.solve();

            System.out.println("Start:");
            System.out.println(start);
            System.out.println();
            System.out.println("Solution: " + solution);
            System.out.println();

            BoardPosition position = start;
            System.out.println("Steps:");
            for (Direction move : solution) {
                position = puzzle.move(position, move);
                System.out.println(move);
                System.out.println(position);
                System.out.println();
            }
        } finally {
            executor.shutdownNow();
        }
    }
}
