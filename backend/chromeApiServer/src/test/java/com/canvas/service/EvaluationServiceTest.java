package com.canvas.service;

import com.canvas.exceptions.CanvasAPIException;
import com.canvas.service.helperServices.CanvasClientService;
import com.canvas.service.helperServices.FileService;
import com.canvas.service.models.ExtensionUser;
import com.canvas.service.models.UserType;
import com.canvas.service.models.submission.Submission;
import com.canvas.service.models.submission.SubmissionFile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Map;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class EvaluationServiceTest {

    private EvaluationService evaluationService;
    private CanvasClientService canvasClientService;
    private FileService fileService;

    @BeforeEach
    public void before() {
        canvasClientService = Mockito.mock(CanvasClientService.class);
        fileService = Mockito.mock(FileService.class);
        evaluationService = new EvaluationService(fileService, canvasClientService);
    }

    @Test
    void generateSubmissionDirectory_shouldReturnResponseEntityWithSubmission() throws CanvasAPIException, IOException {
        // Arrange
        String bearerToken = "authToken";
        String assignmentId = "fooAssignmentId";
        String courseId = "fooCourseId";
        String studentId = "fooStudentId";
        UserType userType = UserType.GRADER;
        String userId = "fooUserId";
        Map<String, byte[]> fileBytes = Map.ofEntries(entry("hello.cpp", "Hello\nWorld".getBytes()));
        String[] fileContent = new String[] {"Hello", "World"};
        String submissionId = "fooSubmissionId";

        Submission submission = Submission.builder()
                .submissionId(submissionId)
                .assignmentId(assignmentId)
                .studentId(studentId)
                .submissionFileBytes(fileBytes)
                .build();
        ExtensionUser user = new ExtensionUser(bearerToken, userId, courseId, assignmentId, studentId, userType);

        Response canvasSubmissionResponse = new Response.Builder()
                .request(new Request.Builder()
                        .url(CanvasClientService.CANVAS_URL + "/foo")
                        .get()
                        .addHeader(CanvasClientService.AUTH_HEADER, bearerToken)
                        .build()
                )
                .protocol(Protocol.HTTP_2)
                .code(401) // status code
                .message("")
                .body(ResponseBody.create(
                        MediaType.get("application/json; charset=utf-8"),
                        "{\"attachments\": [{\"id\": \"fooId\", \"filename\": \"fooFileName\", \"preview_url\": \"foo/bar\"}] }"
                ))
                .build();
        JsonNode canvasSubmJsonNode = new ObjectMapper().readTree(canvasSubmissionResponse.body().string());

        Mockito.when(canvasClientService.fetchCanvasSubmissionJson(user)).thenReturn(canvasSubmJsonNode);
        Mockito.when(canvasClientService.fetchStudentSubmission(user)).thenReturn(submission);
        Mockito.when(canvasClientService.fetchFileUnderCourseAssignmentFolder(any(), any())).thenReturn(null);
        Mockito.doNothing().when(fileService).writeFileFromBytes(any(), any(), any());
        Mockito.when(fileService.parseLinesFromFile(any(), any())).thenReturn(fileContent);

        // Act
        ResponseEntity<Submission> responseEntity = evaluationService.generateSubmissionDirectory(user);
        Submission resultSubmission = responseEntity.getBody();

        // Assert
        assertEquals(HttpStatus.OK.value(), responseEntity.getStatusCode().value());
        assertEquals("fooSubmissionId", resultSubmission.getSubmissionId());
        assertEquals("fooAssignmentId", resultSubmission.getAssignmentId());
        assertEquals("fooStudentId", resultSubmission.getStudentId());
        assertEquals("hello.cpp", resultSubmission.getSubmissionFiles()[0].getName());
        assertArrayEquals(new String[]{"Hello", "World"}, resultSubmission.getSubmissionFiles()[0].getFileContent());
        assertNotNull(resultSubmission.getSubmissionDirectory());
        assertEquals(fileBytes, resultSubmission.getSubmissionFileBytes());

    }
}