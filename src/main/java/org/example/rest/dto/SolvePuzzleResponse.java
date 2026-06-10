package org.example.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record SolvePuzzleResponse(
        @Schema(description = "Whether the puzzle was solved")
        boolean solved,
        @Schema(description = "Sequence of moves that solves the puzzle")
        List<String> moves,
        @Schema(description = "Board states from start to goal")
        List<List<Integer>> boards,
        @Schema(description = "Time spent solving in milliseconds")
        long durationMs,
        @Schema(description = "Human-readable status message")
        String message
) {
    public SolvePuzzleResponse {
        moves = moves == null ? List.of() : List.copyOf(moves);
        boards = boards == null ? List.of() : List.copyOf(boards);
    }
}
