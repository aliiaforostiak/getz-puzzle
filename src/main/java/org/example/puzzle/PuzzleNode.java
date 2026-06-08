package org.example.puzzle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class PuzzleNode<P, M> {
    private final PuzzleNode<P, M> parent;
    private final M move;
    private final P position;

    PuzzleNode(PuzzleNode<P, M> parent, M move, P position) {
        this.parent = parent;
        this.move = move;
        this.position = position;
    }

    P position() {
        return position;
    }

    List<M> asMoveList() {
        List<M> moves = new ArrayList<>();
        for (PuzzleNode<P, M> node = this; node != null && node.move != null; node = node.parent) {
            moves.add(node.move);
        }
        Collections.reverse(moves);
        return moves;
    }
}
