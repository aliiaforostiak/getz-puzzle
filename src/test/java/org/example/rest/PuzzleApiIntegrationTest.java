package org.example.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PuzzleApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void solveEndpointUsesRealServiceAndEngine() throws Exception {
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
                .andExpect(jsonPath("$.moves[1]").value("RIGHT"))
                .andExpect(jsonPath("$.boards[0][6]").value(0))
                .andExpect(jsonPath("$.boards[2][8]").value(0))
                .andExpect(jsonPath("$.message").value("Solved"));
    }

    @Test
    void healthEndpointIsAvailableInFullContext() throws Exception {
        mockMvc.perform(get("/api/puzzles/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void invalidSolveRequestIsRejectedByFullStack() throws Exception {
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
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }
}
