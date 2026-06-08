package org.example.puzzle;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoardPositionTest {

    @Test
    void constructorRejectsNonPositiveSize() {
        assertThrows(IllegalArgumentException.class, () -> new BoardPosition(0, List.of()));
        assertThrows(IllegalArgumentException.class, () -> new BoardPosition(-1, List.of()));
    }

    @Test
    void constructorRejectsWrongTileCount() {
        assertThrows(IllegalArgumentException.class, () -> new BoardPosition(2, List.of(1, 2, 3)));
    }

    @Test
    void blankIndexFailsWhenZeroIsMissing() {
        BoardPosition board = new BoardPosition(2, List.of(1, 2, 3, 4));

        assertThrows(IllegalStateException.class, board::blankIndex);
    }

    @Test
    void tilesAreImmutableCopy() {
        BoardPosition board = new BoardPosition(2, List.of(1, 2, 3, 0));

        assertThrows(UnsupportedOperationException.class, () -> board.tiles().add(9));
    }

    @Test
    void swapCreatesNewBoardWithoutMutatingOriginal() {
        BoardPosition board = new BoardPosition(2, List.of(1, 2, 3, 0));

        BoardPosition swapped = board.swap(2, 3);

        assertNotSame(board, swapped);
        assertEquals(List.of(1, 2, 0, 3), swapped.tiles());
        assertEquals(List.of(1, 2, 3, 0), board.tiles());
    }

    @Test
    void equalsAndHashCodeDependOnSizeAndTiles() {
        BoardPosition left = new BoardPosition(2, List.of(1, 2, 3, 0));
        BoardPosition equal = new BoardPosition(2, List.of(1, 2, 3, 0));
        BoardPosition different = new BoardPosition(2, List.of(1, 2, 0, 3));

        assertEquals(left, equal);
        assertEquals(left.hashCode(), equal.hashCode());
        assertNotEquals(left, different);
        assertEquals(left, left);
    }
}
