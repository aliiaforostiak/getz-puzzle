package org.example.puzzle;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlidingPuzzleTest {

    @Test
    void initialPositionIsReturnedAsIs() {
        BoardPosition start = new BoardPosition(3, List.of(
                1, 2, 3,
                4, 5, 6,
                0, 7, 8
        ));

        SlidingPuzzle puzzle = new SlidingPuzzle(start);

        assertEquals(start, puzzle.initialPosition());
    }

    @Test
    void isGoalRecognizesSolvedBoard() {
        SlidingPuzzle puzzle = new SlidingPuzzle(new BoardPosition(3, List.of(
                1, 2, 3,
                4, 5, 6,
                7, 8, 0
        )));

        assertTrue(puzzle.isGoal(puzzle.initialPosition()));
    }

    @Test
    void isGoalRejectsUnsolvedBoard() {
        SlidingPuzzle puzzle = new SlidingPuzzle(new BoardPosition(3, List.of(
                1, 2, 3,
                4, 5, 6,
                0, 7, 8
        )));

        assertFalse(puzzle.isGoal(puzzle.initialPosition()));
    }

    @Test
    void legalMovesAtCornerAreLimitedToTwoDirections() {
        SlidingPuzzle puzzle = new SlidingPuzzle(new BoardPosition(3, List.of(
                0, 1, 2,
                3, 4, 5,
                6, 7, 8
        )));

        assertEquals(List.of(Direction.DOWN, Direction.RIGHT), puzzle.legalMoves(puzzle.initialPosition()));
    }

    @Test
    void legalMovesAtEdgeIncludeThreeDirections() {
        SlidingPuzzle puzzle = new SlidingPuzzle(new BoardPosition(3, List.of(
                1, 0, 2,
                3, 4, 5,
                6, 7, 8
        )));

        assertEquals(List.of(Direction.DOWN, Direction.LEFT, Direction.RIGHT), puzzle.legalMoves(puzzle.initialPosition()));
    }

    @Test
    void legalMovesAtCenterIncludeAllDirections() {
        SlidingPuzzle puzzle = new SlidingPuzzle(new BoardPosition(3, List.of(
                1, 2, 3,
                4, 0, 5,
                6, 7, 8
        )));

        assertEquals(List.of(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT), puzzle.legalMoves(puzzle.initialPosition()));
    }

    @Test
    void moveReturnsNewPositionWithBlankSwapped() {
        BoardPosition start = new BoardPosition(3, List.of(
                1, 2, 3,
                4, 5, 6,
                0, 7, 8
        ));
        SlidingPuzzle puzzle = new SlidingPuzzle(start);

        BoardPosition moved = puzzle.move(start, Direction.RIGHT);

        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 0, 8), moved.tiles());
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 0, 7, 8), start.tiles());
    }
}
