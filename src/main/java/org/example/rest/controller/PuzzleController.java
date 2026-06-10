package org.example.rest.controller;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.rest.dto.SolvePuzzleRequest;
import org.example.rest.dto.SolvePuzzleResponse;
import org.example.rest.service.PuzzleSolverService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/puzzles")
@Tag(name = "Puzzle", description = "Endpoints for solving sliding puzzles")
public class PuzzleController {
    private final PuzzleSolverService puzzleSolverService;

    public PuzzleController(PuzzleSolverService puzzleSolverService) {
        this.puzzleSolverService = puzzleSolverService;
    }

    @PostMapping(value = "/solve", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Solve a sliding puzzle")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Puzzle solved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SolvePuzzleResponse.class),
                            examples = @ExampleObject("""
                                    {
                                      "solved": true,
                                      "moves": ["RIGHT", "RIGHT"],
                                      "boards": [
                                        [1,2,3,4,5,6,0,7,8],
                                        [1,2,3,4,5,6,7,0,8],
                                        [1,2,3,4,5,6,7,8,0]
                                      ],
                                      "durationMs": 12,
                                      "message": "Solved"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request payload",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RestExceptionHandler.ApiErrorResponse.class),
                            examples = @ExampleObject("""
                                    {
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "timeoutSeconds must be greater than or equal to 1",
                                      "timestamp": "2026-06-10T08:00:00Z",
                                      "details": []
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "408",
                    description = "Solver timed out",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RestExceptionHandler.ApiErrorResponse.class),
                            examples = @ExampleObject("""
                                    {
                                      "status": 408,
                                      "error": "Request Timeout",
                                      "message": "No solution found within 15 seconds",
                                      "timestamp": "2026-06-10T08:00:00Z",
                                      "details": []
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RestExceptionHandler.ApiErrorResponse.class),
                            examples = @ExampleObject("""
                                    {
                                      "status": 500,
                                      "error": "Internal Server Error",
                                      "message": "Unexpected server error",
                                      "timestamp": "2026-06-10T08:00:00Z",
                                      "details": []
                                    }
                                    """)
                    )
            )
    })
    public ResponseEntity<SolvePuzzleResponse> solve(@Valid @RequestBody SolvePuzzleRequest request) {
        return ResponseEntity.ok(puzzleSolverService.solve(request));
    }

    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Check application health")
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(new HealthResponse("UP"));
    }

    public record HealthResponse(String status) {
    }
}
