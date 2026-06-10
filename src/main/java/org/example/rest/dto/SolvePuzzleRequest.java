package org.example.rest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "SolvePuzzleRequest", description = "Request body for solving a sliding puzzle")
public record SolvePuzzleRequest(
        @Schema(description = "Size of one board side", example = "3")
        @Min(1) int size,
        @Schema(description = "Tiles in row-major order, where 0 is the blank tile", example = "[1,2,3,4,5,6,0,7,8]")
        @NotNull List<@NotNull Integer> tiles,
        @Schema(description = "Maximum waiting time for the solver in seconds", example = "15")
        @Min(1) Long timeoutSeconds
) {
    public SolvePuzzleRequest {
        tiles = tiles == null ? null : List.copyOf(tiles);
    }
}
