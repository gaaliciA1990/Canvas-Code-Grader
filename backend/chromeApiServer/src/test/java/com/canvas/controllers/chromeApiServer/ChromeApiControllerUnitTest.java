package com.canvas.controllers.chromeApiServer;

import com.canvas.exceptions.CanvasAPIException;
import com.canvas.exceptions.CanvasAPIException;
import com.canvas.exceptions.IncorrectRequestParamsException;
import com.canvas.service.EvaluationService;
import com.canvas.service.SubmissionDirectoryService;
import com.canvas.service.models.CommandOutput;
import com.canvas.service.helperServices.CanvasClientService;
import com.canvas.service.models.ExtensionUser;
import com.canvas.service.models.UserType;
import com.canvas.service.models.submission.Deletion;
import com.canvas.service.models.submission.Submission;
import com.canvas.service.models.submission.SubmissionFile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.catalina.User;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;


import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest
class ChromeApiControllerUnitTest {

    /**
     * automatically wire up the dependencies for the test class
     */
    @Autowired
    MockMvc mockMvc;

    @MockBean
    ExtensionUser user;

    @MockBean
    CanvasClientService canvasClientService;

    @MockBean
    EvaluationService evaluationService;

    @MockBean
    SubmissionDirectoryService submissionDirectoryService;

    ChromeApiController controller;

    /**
     * Placeholder for action to take before running tests
     */
    @BeforeEach
    void setUp() {
        controller = new ChromeApiController(evaluationService, canvasClientService, submissionDirectoryService);
    }

    /**
     * Placeholder for action to take after running tests
     */
    @AfterEach
    void tearDown() {

    }

    /**
     * Tests ResponseEntity returns OK with correct params
     */
    @Test
    public void initiateStudentCodeEvaluation_should_return_responseEntity_OK() throws Exception {
        // Set Up
        String authToken = "Iamastringtoken";
        MockMultipartFile multipartFile = new MockMultipartFile("files", "test.txt",
                "text/plain", "Spring Framework".getBytes());
        String userID = "132546";
        String assignmentID = "cs5461321";
        String courseID = "cpsc5023";
        UserType type = UserType.STUDENT;
        CommandOutput commandOutput = new CommandOutput(true, new String[] {"test"});

        Mockito.when(canvasClientService.fetchUserId(authToken)).thenReturn(userID);
        Mockito.when(evaluationService.compileStudentCodeFile(any(), any())).thenReturn(new ResponseEntity<>(commandOutput, HttpStatus.OK));

        // Act
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart("/evaluate")
                        .file(multipartFile)
                        .header("Authorization", authToken)
                        .param("assignmentId", assignmentID)
                        .param("courseId", courseID)
                        .param("userType", String.valueOf(type))
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())  // assert we get OK response
                .andReturn();
        System.out.println("RESPONSE " + result.getResponse().getContentAsString());

        // Assert
        CommandOutput controllerResponse = new ObjectMapper().readValue(result.getResponse().getContentAsString(),
                CommandOutput.class);
        assertTrue(controllerResponse.isSuccess());
    }

    /**
     * Tests the initiateStudentCodeEvaluation method return Bad_Request for wrong userTypes
     *
     * @param userType Enum UserType
     * @throws Exception exception
     */
    @ParameterizedTest
    @CsvFileSource(resources = "/chromeAPI_userType_student_tests.csv", numLinesToSkip = 1)
    public void initiateStudentCodeEvaluation_should_return_badRequest_when_userType_not_valid_enum(String userType) throws Exception {
        // Set Up
        String authToken = "TestStringToken";
        MockMultipartFile multipartFile = new MockMultipartFile("files", "test.txt",
                "text/plain", "Spring Framework".getBytes());
        String userID = "132546";
        String assignmentID = "cs5461321";
        String courseID = "cpsc5023";
        String type = userType;

        // Act
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart("/evaluate")
                        .file(multipartFile)
                        .header("Authorization", authToken)
                        .param("assignmentId", assignmentID)
                        .param("courseId", courseID)
                        .param("userType", type)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())  // assert we get bad request response
                .andReturn();

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }

    @ParameterizedTest
    @ValueSource(strings = {"UNAUTHORIZED", "GRADER"})
    public void initiateStudentCodeEvaluation_should_return_unauthorized_when_userType_not_STUDENT(String userType) throws Exception {
        // Set Up
        String authToken = "TestStringToken";
        MockMultipartFile multipartFile = new MockMultipartFile("files", "test.txt",
                "text/plain", "Spring Framework".getBytes());
        String userID = "132546";
        String assignmentID = "cs5461321";
        String courseID = "cpsc5023";
        String type = userType;

        // Act
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart("/evaluate")
                        .file(multipartFile)
                        .header("Authorization", authToken)
                        .param("assignmentId", assignmentID)
                        .param("courseId", courseID)
                        .param("userType", type)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())  // assert we get unauthorized response
                .andReturn();

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
    }

    @Test
    public void getOAuth2Response_should_return_login_success_when_access_token_is_returned() throws Exception {
        // Set Up
        String code = "testCode";
        Mockito.when(canvasClientService.fetchAccessTokenResponse(any(), any(), any(), any())).thenReturn(new Response.Builder()
                .request(new Request.Builder()
                        .url(CanvasClientService.CANVAS_URL + "/oauthResponse")
                        .get()
                        .build())
                .protocol(Protocol.HTTP_2)
                .code(200) // status code
                .message("")
                .body(ResponseBody.create(
                        okhttp3.MediaType.get("application/json; charset=utf-8"),
                        "{\"access_token\": \"TestAccessToken\" }"
                )).build());


        // Act
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/oauth2Response")
                        .param("code", code)).andReturn();

        // Assert
        assertEquals(HttpStatus.FOUND.value(), result.getResponse().getStatus());
        assertEquals("/loginSuccess?access_token=TestAccessToken", result.getResponse().getRedirectedUrl());
    }

    @Test
    public void getOAuth2Response_should_return_bad_request_when_code_is_not_passed() throws Exception {

        // Act
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/oauth2Response")).andReturn();

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }
    @Test
    public void getOAuth2Response_should_return_login_failure_when_access_token_response_fails() throws Exception {
        // Set Up
        Mockito.when(canvasClientService.fetchAccessTokenResponse(any(), any(), any(), any())).thenThrow(new CanvasAPIException());


        // Act
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/oauth2Response")
                .param("code", "testCode")).andReturn();

        // Assert
        assertEquals(HttpStatus.FOUND.value(), result.getResponse().getStatus());
        assertEquals("/loginFail", result.getResponse().getRedirectedUrl());
    }
    @ParameterizedTest
    @ValueSource(ints = {302, 401, 200})
    public void getOAuth2Response_should_return_login_failure_when_access_token_response_is_not_valid(int code) throws Exception {
        // Set Up
        Mockito.when(canvasClientService.fetchAccessTokenResponse(any(), any(), any(), any())).thenReturn(new Response.Builder()
                .request(new Request.Builder()
                        .url(CanvasClientService.CANVAS_URL + "/oauthResponse")
                        .get()
                        .build())
                .protocol(Protocol.HTTP_2)
                .code(code) // status code
                .message("")
                .body(ResponseBody.create(
                        okhttp3.MediaType.get("application/json; charset=utf-8"),
                        "{\"message\": \"Unauthorized\" }"
                )).build());


        // Act
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/oauth2Response")
                .param("code", "testCode")).andReturn();

        // Assert
        assertEquals(HttpStatus.FOUND.value(), result.getResponse().getStatus());
        assertEquals("/loginFail", result.getResponse().getRedirectedUrl());
    }

    @Test
    public void getLoginFail_should_succeed() throws Exception {

        // Act
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/loginFail")).andReturn();

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
    }

    @Test
    public void getLoginSuccess_should_succeed() throws Exception {

        // Act
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/loginSuccess")).andReturn();

        // Assert
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertEquals("Login success", result.getResponse().getContentAsString());
    }

    @Test
    public void retrieveStudentSubmissionFiles_shouldReturnResponseEntityOK() throws Exception {
        // Arrange
        String bearerToken = "authToken";
        String assignmentId = "fooAssignmentId";
        String courseId = "fooCourseId";
        String studentId = "fooStudentId";
        UserType userType = UserType.GRADER;
        String userId = "fooUserId";
        Map<String, byte[]> fileBytes = Map.ofEntries(entry("testName", "test".getBytes()));
        SubmissionFile[] submissionFiles = new SubmissionFile[] {
                new SubmissionFile("hello.cpp", new String[] {"Hello", "World"} )
        };
        String submissionId = "fooSubmissionId";
        String submissionDirectory = "fooSubmissionDirectory";

        String endpoint = String.format("/submission/courses/%s/assignments/%s", courseId, assignmentId);
        Submission submission = new Submission(
                submissionId, studentId, assignmentId, fileBytes, submissionFiles, submissionDirectory
        );

        Mockito.when(canvasClientService.fetchUserId(bearerToken)).thenReturn(userId);
        Mockito.when(submissionDirectoryService.generateSubmissionDirectory(any()))
                .thenReturn(new ResponseEntity<>(submission, HttpStatus.OK));

        // Act
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint)
                        .header("Authorization", bearerToken)
                        .param("userType", String.valueOf(userType))
                        .param("studentId", studentId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())  // assert we get OK response
                .andReturn();

        Submission submissionResp = new ObjectMapper()
                .readValue(result.getResponse().getContentAsString(), Submission.class);

        // Assert
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertEquals("fooSubmissionId", submissionResp.getSubmissionId());
        assertEquals("fooAssignmentId", submissionResp.getAssignmentId());
        assertEquals("fooStudentId", submissionResp.getStudentId());
        assertEquals("hello.cpp", submissionResp.getSubmissionFiles()[0].getName());
        assertArrayEquals(new String[]{"Hello", "World"}, submissionResp.getSubmissionFiles()[0].getFileContent());
        assertEquals("fooSubmissionDirectory", submissionResp.getSubmissionDirectory());
        assertNull(submissionResp.getSubmissionFileBytes());
    }

    @ParameterizedTest
    @ValueSource(strings = {"UNAUTHORIZED", "STUDENT"})
    void retrieveStudentSubmissionFiles_shouldThrowUserNotAuthorizedException_whenUserTypeNotGrader(String userType) throws Exception {
        // Arrange
        String bearerToken = "authToken";
        String assignmentId = "fooAssignmentId";
        String courseId = "fooCourseId";
        String studentId = "fooStudentId";
        String endpoint = String.format("/submission/courses/%s/assignments/%s", courseId, assignmentId);

        // Act
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint)
                        .header("Authorization", bearerToken)
                        .param("userType", userType)
                        .param("studentId", studentId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())  // assert we get OK response
                .andReturn();

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
    }

    @ParameterizedTest
    @MethodSource
    void retrieveStudentSubmissionFiles_shouldThrowIncorrectRequestParamsException_whenAnyParameterIsNull(
            String bearerToken,
            String assignmentId,
            String courseId,
            String studentId,
            UserType userType
    ) {
        // Act
        IncorrectRequestParamsException exception = assertThrows(
                IncorrectRequestParamsException.class,
                () -> controller.retrieveStudentSubmissionFiles(bearerToken, assignmentId, courseId, studentId, userType),
                "Expected IncorrectRequestParamsException to be thrown."
        );

        // Assert
        assertEquals(
                "Incorrect request parameters received. Check the parameters meet the requirements",
                exception.getMessage()
        );
    }

    @ParameterizedTest
    @MethodSource
    void retrieveStudentSubmissionFiles_shouldThrowIncorrectRequestParamsException_whenTokenOrAnyIdIsEmptyString(
            String bearerToken,
            String assignmentId,
            String courseId,
            String studentId,
            UserType userType
    ) {
        // Act
        IncorrectRequestParamsException exception = assertThrows(
                IncorrectRequestParamsException.class,
                () -> controller.retrieveStudentSubmissionFiles(bearerToken, assignmentId, courseId, studentId, userType),
                "Expected IncorrectRequestParamsException to be thrown."
        );

        // Assert
        assertEquals(
                "Incorrect request parameters received. Check the parameters meet the requirements",
                exception.getMessage()
        );
    }

    @Test
    void deleteStudentSubmissionFiles_shouldReturnResponseEntityOK() throws Exception {
        // Given
        String endpoint = "/submission/courses/fooCourseId/assignments/fooAssignmentId";
        Deletion deletion = new Deletion(true, "fooDescription");

        Mockito.when(canvasClientService.fetchUserId("fooBearerToken")).thenReturn("fooUserId");
        Mockito.when(submissionDirectoryService.deleteSubmissionDirectory(any()))
                .thenReturn(new ResponseEntity<>(deletion, HttpStatus.OK));

        // Act
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete(endpoint)
                        .header("Authorization", "fooBearerToken")
                        .param("userType", String.valueOf(UserType.GRADER))
                        .param("studentId", "fooUserId")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())  // assert we get OK response
                .andReturn();

        Deletion deletionResp = new ObjectMapper()
                .readValue(result.getResponse().getContentAsString(), Deletion.class);

        Assertions.assertEquals("fooDescription", deletion.getDescription());
        Assertions.assertTrue(deletion.isSuccess());
    }

    /******************** Static methods for providing parameters to parameterized tests ***************************/

    private static Stream<Arguments> retrieveStudentSubmissionFiles_shouldThrowIncorrectRequestParamsException_whenAnyParameterIsNull() {
        return Stream.of(
                Arguments.of(null, "fooAssignmentId", "fooCourseId", "fooStudentId", UserType.GRADER),
                Arguments.of("fooBearerToken", null, "fooCourseId", "fooStudentId", UserType.GRADER),
                Arguments.of("fooBearerToken", "fooAssignmentId", null, "fooStudentId", UserType.GRADER),
                Arguments.of("fooBearerToken", "fooAssignmentId", "fooCourseId", null, UserType.GRADER),
                Arguments.of("fooBearerToken", "fooAssignmentId", "fooCourseId", "fooStudentId", null),
                Arguments.of(null, null, null, null, null)
        );
    }

    private static Stream<Arguments> retrieveStudentSubmissionFiles_shouldThrowIncorrectRequestParamsException_whenTokenOrAnyIdIsEmptyString() {
        return Stream.of(
                Arguments.of("", "fooAssignmentId", "fooCourseId", "fooStudentId", UserType.GRADER),
                Arguments.of("fooBearerToken", "", "fooCourseId", "fooStudentId", UserType.GRADER),
                Arguments.of("fooBearerToken", "fooAssignmentId", "", "fooStudentId", UserType.GRADER),
                Arguments.of("fooBearerToken", "fooAssignmentId", "fooCourseId", "", UserType.GRADER),
                Arguments.of("", "", "", "", UserType.GRADER)
        );
    }
}