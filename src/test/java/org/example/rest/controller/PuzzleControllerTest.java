package org.example.rest.controller;

import org.example.rest.dto.SolvePuzzleResponse;
import org.example.rest.service.PuzzleSolverService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PuzzleController.class)
@Import({RestExceptionHandler.class, PuzzleControllerTest.MockConfig.class})
class PuzzleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PuzzleSolverService puzzleSolverService;

    @AfterEach
    void resetMock() {
        reset(puzzleSolverService);
    }

    @Test
    void solveReturnsSolvedResponse() throws Exception {
        doReturn(new SolvePuzzleResponse(
                true,
                List.of("RIGHT", "RIGHT"),
                List.of(
                        List.of(1, 2, 3, 4, 5, 6, 0, 7, 8),
                        List.of(1, 2, 3, 4, 5, 6, 7, 0, 8),
                        List.of(1, 2, 3, 4, 5, 6, 7, 8, 0)
                ),
                12L,
                "Solved"
        )).when(puzzleSolverService).solve(any());

        mockMvc.perform(post("/api/puzzles/solve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "size": 3,
                                  "tiles": [1,2,3,4,5,6,0,7,8],
                                  "timeoutSeconds": 15
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.solved").value(true))
                .andExpect(jsonPath("$.moves[0]").value("RIGHT"))
                .andExpect(jsonPath("$.boards[2][8]").value(0))
                .andExpect(jsonPath("$.message").value("Solved"));

        verify(puzzleSolverService).solve(any());
    }

    @Test
    void solveReturnsBadRequestForInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/puzzles/solve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "size": 0,
                                  "tiles": [1,2,3],
                                  "timeoutSeconds": 15
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void solveMapsIllegalArgumentExceptionToBadRequest() throws Exception {
        doThrow(new IllegalArgumentException("tiles must contain the blank tile 0"))
                .when(puzzleSolverService).solve(any());

        mockMvc.perform(post("/api/puzzles/solve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "size": 3,
                                  "tiles": [1,2,3,4,5,6,7,8,1],
                                  "timeoutSeconds": 15
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("tiles must contain the blank tile 0"));
    }

    @Test
    void solveMapsTimeoutToRequestTimeout() throws Exception {
        doThrow(new PuzzleSolveTimeoutException(15L))
                .when(puzzleSolverService).solve(any());

        mockMvc.perform(post("/api/puzzles/solve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "size": 3,
                                  "tiles": [1,2,3,4,5,6,0,7,8],
                                  "timeoutSeconds": 15
                                }
                                """))
                .andExpect(status().isRequestTimeout())
                .andExpect(jsonPath("$.status").value(408))
                .andExpect(jsonPath("$.error").value("Request Timeout"));
    }

    @Test
    void healthReturnsUp() throws Exception {
        mockMvc.perform(get("/api/puzzles/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @TestConfiguration
    static class MockConfig {
        @Bean
        PuzzleSolverService puzzleSolverService() {
            return org.mockito.Mockito.mock(PuzzleSolverService.class);
        }
    }
}
