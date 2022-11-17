package com.canvas.chromeApiServer;

import com.canvas.dto.CommandOutput;
import com.fasterxml.jackson.databind.ObjectMapper;
import kotlin.Unit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest
class ChromeApiControllerUnitTest {

    /**
     * automatically wire up the dependencies for the test class
     */
    @Autowired
    MockMvc mockMvc;

    /**
     * Placeholder for action to take before running tests
     */
    @BeforeEach
    void setUp() {

    }

    /**
     * Placeholder for action to take after running tests
     */
    @AfterEach
    void tearDown() {

    }

    /**
     * Tests ResponseEntity returns OK with params
     */
    @Test
    public void should_return_responseEntity_OK() throws Exception {
        // Set Up
        String authToken = "Iamastringotken";
        MockMultipartFile multipartFile = new MockMultipartFile("files", "test.txt",
                "text/plain", "Spring Framework".getBytes());
        String userID = "132546";
        String assignmentID = "cs5461321";
        String courseID = "cpsc5023";

        // Act
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart("/evaluate")
                        .file(multipartFile)
                        .header("Authorization", authToken)
                        .param("userId", userID)
                        .param("assignmentId", assignmentID)
                        .param("courseId", courseID)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())  // assert we get OK response
                .andReturn();

        // Assert
        CommandOutput controllerResponse = new ObjectMapper().readValue(result.getResponse().getContentAsString(), CommandOutput.class);
        assertEquals(true, controllerResponse.isSuccess());
    }


}