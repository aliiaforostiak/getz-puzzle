package org.example.puzzle;

import java.util.ArrayList;
import java.util.List;

public final class SlidingPuzzle implements Puzzle<BoardPosition, Direction> {
    private final BoardPosition initialPosition;

    public SlidingPuzzle(BoardPosition initialPosition) {
        this.initialPosition = initialPosition;
    }

    @Override
    public BoardPosition initialPosition() {
        return initialPosition;
    }

    @Override
    public boolean isGoal(BoardPosition position) {
        List<Integer> tiles = position.tiles();
        for (int index = 0; index < tiles.size() - 1; index++) {
            if (tiles.get(index) != index + 1) {
                return false;
            }
        }
        return tiles.getLast() == 0;
    }

    @Override
    public List<Direction> legalMoves(BoardPosition position) {
        int size = position.size();
        int blankIndex = position.blankIndex();
        int blankRow = blankIndex / size;
        int blankColumn = blankIndex % size;
        List<Direction> moves = new ArrayList<>(4);
        for (Direction move : Direction.values()) {
            int nextRow = blankRow + move.rowDelta();
            int nextColumn = blankColumn + move.columnDelta();
            if (nextRow >= 0 && nextRow < size && nextColumn >= 0 && nextColumn < size) {
                moves.add(move);
            }
        }
        return moves;
    }

    @Override
    public BoardPosition move(BoardPosition position, Direction move) {
        int size = position.size();
        int blankIndex = position.blankIndex();
        int blankRow = blankIndex / size;
        int blankColumn = blankIndex % size;
        int nextRow = blankRow + move.rowDelta();
        int nextColumn = blankColumn + move.columnDelta();
        int targetIndex = nextRow * size + nextColumn;
        return position.swap(blankIndex, targetIndex);
    }
}
