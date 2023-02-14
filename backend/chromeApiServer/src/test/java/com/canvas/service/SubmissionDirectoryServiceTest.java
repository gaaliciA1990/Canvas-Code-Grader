package com.canvas.service;

import com.canvas.exceptions.CanvasAPIException;
import com.canvas.service.helperServices.CanvasClientService;
import com.canvas.service.helperServices.FileService;
import com.canvas.service.models.ExtensionUser;
import com.canvas.service.models.UserType;
import com.canvas.service.models.submission.Submission;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class SubmissionDirectoryServiceTest {
    private CanvasClientService canvasClientService;
    private SubmissionDirectoryService submissionDirectoryService;

    @Nested
    class TestsWithMockFileService {

        private FileService fileService;


        @BeforeEach
        public void before() {
            canvasClientService = Mockito.mock(CanvasClientService.class);
            fileService = Mockito.mock(FileService.class);
            submissionDirectoryService = new SubmissionDirectoryService(fileService, canvasClientService);
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
            ResponseEntity<Submission> responseEntity = submissionDirectoryService.generateSubmissionDirectory(user);
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

    @Nested
    class TestsWithNormalFileService {

        private FileService fileService;
        private SubmissionDirectoryService submissionDirectoryService;

        @BeforeEach
        public void before() {
            canvasClientService = Mockito.mock(CanvasClientService.class);
            fileService = new FileService();
            submissionDirectoryService = new SubmissionDirectoryService(fileService, canvasClientService);
        }

        @Test
        void writeMakefile_shouldWriteFileToDirectory() throws IOException, CanvasAPIException {
            // Arrange
            String userId = "fooUserId";
            ExtensionUser user = new ExtensionUser(
                    userId,
                    "fooUserId",
                    "fooCourseId",
                    "fooAssignmentId",
                    "fooStudentId",
                    UserType.GRADER
            );
            byte[] bytes = {1, 2, 3};
            when(canvasClientService.fetchFileUnderCourseAssignmentFolder(any(), any())).thenReturn(bytes);

            // Act
            submissionDirectoryService.writeMakefileFromCanvas(user);

            // Get Contents
            byte[] data = Files.readAllBytes(Path.of("./" + userId + "/makefile"));

            // Verify
            for (int i=0; i<3; i++) {
                assertEquals(bytes[i], data[i]);
            }

            // Delete directory
            Files.deleteIfExists(Path.of("./" + userId + "/makefile"));
            File directory = new File(userId);
            directory.delete();
        }

        @Test
        void deleteDirectory_shouldDeleteDirectory() {
            // Arrange
            File dir = new File("./fooId");
            dir.mkdirs();

            // Act
            submissionDirectoryService.deleteDirectory("fooId");

            // Assert
            assertFalse(dir.exists());
        }

        @Test
        void getSubmissionDirectory_shouldReturnSubmissionDirectoryName() {
            // Arrange
            File dir = new File("./fooId");
            dir.mkdirs();

            // Act/Assert
            assertEquals("./fooId", submissionDirectoryService.getSubmissionDirectory("fooId"));

            // Cleanup
            fileService.deleteDirectory("fooId");
        }

        @Test
        void writeSubmissionFiles_shouldWriteMultipartFilesToSubmissionDirectory() throws IOException {
            MultipartFile multipartFile = Mockito.mock(MultipartFile.class);
            byte[] bytes = {1, 2, 3};

            // TODO: original method is dependent on a file directory already being created. Is this correct?
            File dir = new File("./fooId");
            dir.mkdirs();

            when(multipartFile.getBytes()).thenReturn(bytes);
            when(multipartFile.getOriginalFilename()).thenReturn("fooOriginalFileName.txt");

            submissionDirectoryService.writeSubmissionFiles(new MultipartFile[] {multipartFile}, "fooId");

            // Verify contents
            byte[] data = Files.readAllBytes(Path.of("./fooId/fooOriginalFileName.txt"));

            for (int i=0; i<3; i++) {
                assertEquals(bytes[i], data[i]);
            }

            // Delete directory
            Files.deleteIfExists(Path.of("./fooId/fooOriginalFileName.txt"));
            File directory = new File("fooId");
            directory.delete();
        }

        @Test
        public void testDeleteSubmissionDirectory() {
            // Given
            ExtensionUser user = new ExtensionUser(
                    "fooToken",
                    "fooId",
                    "fooCourse",
                    "fooAssignment",
                    "fooId",
                    UserType.STUDENT);
            String uniqueDirectoryName = submissionDirectoryService.generateUniqueDirectoryName(
                    user.getCourseId(),
                    user.getAssignmentId(),
                    user.getStudentId()
            );
            File dir = new File(uniqueDirectoryName);
            dir.mkdirs();
            // Assert exists
            Assertions.assertTrue(dir.exists());

            // When
            submissionDirectoryService.deleteSubmissionDirectory(user);

            // Then
            // Assert does not exist
            Assertions.assertFalse(dir.exists());
        }
    }



}