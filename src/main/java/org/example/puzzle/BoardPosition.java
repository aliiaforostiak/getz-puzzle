package org.example.puzzle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class BoardPosition {
    private final int size;
    private final List<Integer> tiles;

    public BoardPosition(int size, List<Integer> tiles) {
        if (size <= 0) {
            throw new IllegalArgumentException("size must be positive");
        }
        if (tiles.size() != size * size) {
            throw new IllegalArgumentException("tile count must equal size squared");
        }
        this.size = size;
        this.tiles = List.copyOf(tiles);
    }

    public int size() {
        return size;
    }

    public List<Integer> tiles() {
        return tiles;
    }

    public int blankIndex() {
        int blankIndex = tiles.indexOf(0);
        if (blankIndex < 0) {
            throw new IllegalStateException("board does not contain a blank tile");
        }
        return blankIndex;
    }

    public BoardPosition swap(int firstIndex, int secondIndex) {
        List<Integer> nextTiles = new ArrayList<>(tiles);
        Collections.swap(nextTiles, firstIndex, secondIndex);
        return new BoardPosition(size, nextTiles);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof BoardPosition that)) {
            return false;
        }
        return size == that.size && tiles.equals(that.tiles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(size, tiles);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int row = 0; row < size; row++) {
            for (int column = 0; column < size; column++) {
                int value = tiles.get(row * size + column);
                builder.append(value == 0 ? " ." : String.format("%2d", value));
                if (column < size - 1) {
                    builder.append(' ');
                }
            }
            if (row < size - 1) {
                builder.append(System.lineSeparator());
            }
        }
        return builder.toString();
    }
}
