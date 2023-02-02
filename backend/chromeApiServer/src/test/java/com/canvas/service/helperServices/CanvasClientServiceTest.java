package com.canvas.service.helperServices;

import com.canvas.exceptions.CanvasAPIException;
import com.canvas.service.models.ExtensionUser;
import com.canvas.service.models.UserType;
import com.canvas.service.models.submission.Submission;
import com.fasterxml.jackson.databind.JsonNode;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import static org.mockito.Mockito.*;

class CanvasClientServiceTest {

    private static final String BEARER_TOKEN = "fooToken";
    private static final String USER_ID = "fooId";
    private static final Request USER_ID_REQUEST =
            new Request.Builder()
                    .url(CanvasClientService.CANVAS_URL + "/users/self")
                    .get()
                    .addHeader(CanvasClientService.AUTH_HEADER, BEARER_TOKEN)
                    .build();

    private final Response response = new Response.Builder()
            .request(USER_ID_REQUEST)
            .protocol(Protocol.HTTP_2)
            .code(401) // status code
            .message("")
            .body(ResponseBody.create(
                    MediaType.get("application/json; charset=utf-8"),
                    "{\"id\":\"fooId\", \"assignment\": {\"name\": \"fooAssignmentId\", \"files_url\": \"http://fooUrl.foo\", \"id\": 12345} }"
            ))
            .build();

    private final Response filesResponse1 = new Response.Builder()
            .request(USER_ID_REQUEST)
            .protocol(Protocol.HTTP_2)
            .code(401) // status code
            .message("")
            .body(ResponseBody.create(
                    MediaType.get("application/json; charset=utf-8"),
                    "{\"id\":\"fooId\", \"assignment\": {\"display_name\": \"fooAssignmentId\", \"id\": 12345} }"
            ))
            .build();



    private final Response filesResponse2 = new Response.Builder()
            .request(USER_ID_REQUEST)
            .protocol(Protocol.HTTP_2)
            .code(401) // status code
            .message("")
            .body(ResponseBody.create(
                    MediaType.get("application/json; charset=utf-8"),
                    "{\"id\":\"fooId\", \"url\": \"http://fooUrl.foo\", \"assignment\": {\"files_url\": \"http://fooUrl.foo\"} }"
            ))
            .build();

    private final Response filesResponse3 = new Response.Builder()
            .request(USER_ID_REQUEST)
            .protocol(Protocol.HTTP_2)
            .code(401) // status code
            .message("")
            .body(ResponseBody.create(
                    MediaType.get("application/json; charset=utf-8"),
                    "{\"id\":\"fooId\", \"url\": \"http://fooUrl.foo\", \"assignment\": {\"files_url\": \"http://fooUrl.foo\"} }"
            ))
            .build();

    private final Response attachmentResponse = new Response.Builder()
            .request(USER_ID_REQUEST)
            .protocol(Protocol.HTTP_2)
            .code(401) // status code
            .message("")
            .body(ResponseBody.create(
                    MediaType.get("application/json; charset=utf-8"),
                    "{\"attachments\": [{\"id\": \"fooId\", \"filename\": \"fooFileName\"}] }"
            ))
            .build();

    private final Response canvasSubmissionResponse = new Response.Builder()
            .request(USER_ID_REQUEST)
            .protocol(Protocol.HTTP_2)
            .code(401) // status code
            .message("")
            .body(ResponseBody.create(
                    MediaType.get("application/json; charset=utf-8"),
                    "{\"id\": \"fooSubmissionId\", \"attachments\": [{\"id\": \"fooId\", \"filename\": \"fooFileName\", \"preview_url\": \"foo/bar\"}] }"
            ))
            .build();

    private final Response fetchFileResponse = new Response.Builder()
            .request(USER_ID_REQUEST)
            .protocol(Protocol.HTTP_2)
            .code(401) // status code
            .message("")
            .body(ResponseBody.create(
                    MediaType.get("application/json; charset=utf-8"),
                    "{\"url\": \"http://foo/bar/url\"}] }"
            ))
            .build();

    private final Response fileUrlResponse = new Response.Builder()
            .request(USER_ID_REQUEST)
            .protocol(Protocol.HTTP_2)
            .code(401) // status code
            .message("")
            .body(ResponseBody.create(
                    MediaType.get("application/json; charset=utf-8"),
                    "{\"foo\": \"bar\"}]}"
            ))
            .build();

    private final Response unAuthorizedAccessTokenResponse = new Response.Builder()
            .request(USER_ID_REQUEST)
            .protocol(Protocol.HTTP_2)
            .code(401) // status code
            .message("")
            .body(ResponseBody.create(
                    MediaType.get("application/json; charset=utf-8"),
                    "{\"message\": \"UnAuthorized\" }"
            ))
            .build();

    private final Response successAccessTokenResponse = new Response.Builder()
            .request(USER_ID_REQUEST)
            .protocol(Protocol.HTTP_2)
            .code(200) // status code
            .message("")
            .body(ResponseBody.create(
                    MediaType.get("application/json; charset=utf-8"),
                    "{\"access_token\": \"xxxxxxyy\" }"
            ))
            .build();
    private final Response emptyResponse = new Response.Builder()
            .request(USER_ID_REQUEST)
            .protocol(Protocol.HTTP_2)
            .code(401) // status code
            .message("")
            .body(ResponseBody.create(
                    MediaType.get("application/json; charset=utf-8"),
                    "{}"
            ))
            .build();

    private final ExtensionUser extensionUser = new ExtensionUser(
            "fooToken",
            "fooUserId",
            "fooCourseId",
            "fooAssignmentId",
            "fooStudentId",
            UserType.GRADER
    );

    private CanvasClientService canvasClientService;

    @Mock
    Call call;

    @Mock
    OkHttpClient okHttpClient;

    @Captor
    ArgumentCaptor<Request> requestCaptor; // TODO: Verify contents of requests

    @BeforeEach
    public void before() {
        MockitoAnnotations.openMocks(this);
        canvasClientService =  new CanvasClientService(okHttpClient);
    }

    @Test
    public void testFetchUserId() throws CanvasAPIException, IOException {

        when(okHttpClient.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(response);

        Assertions.assertEquals(
                USER_ID,
                canvasClientService.fetchUserId(BEARER_TOKEN)
        );
    }

    @Test
    public void testFetchUserId_throwsCanvasAPIException() {

        when(okHttpClient.newCall(any())).thenReturn(null);

        Assertions.assertThrows(
                CanvasAPIException.class,
                () -> canvasClientService.fetchUserId(BEARER_TOKEN)
        );
    }

    @Test
    public void testFetchFileUnderCourse() throws IOException, CanvasAPIException {
        when(okHttpClient.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(response);

        JsonNode node = canvasClientService.fetchFoldersUnderCourse("fooCourse", "fooToken");

        Assertions.assertEquals("fooId", node.get("id").asText());
    }

    @Test
    public void testFetchFileUnderCourse_throwsCanvasAPIException() throws IOException, CanvasAPIException {
        when(okHttpClient.newCall(any())).thenReturn(null);

        Assertions.assertThrows(
                CanvasAPIException.class,
                () -> canvasClientService.fetchFoldersUnderCourse("fooCourse", "fooToken")
        );
    }

    @Test
    public void testFetchFileUnderCourseAssignmentFolder() throws CanvasAPIException, IOException {


        when(okHttpClient.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(response, filesResponse1, filesResponse2, filesResponse3);

        byte[] fileBytes = canvasClientService.fetchFileUnderCourseAssignmentFolder(extensionUser, "fooAssignmentId");
        Assertions.assertEquals(93, fileBytes.length);
    }

    @Test
    public void testFetchFileUnderCourseAssignmentFolder_throwsCanvasAPIException() throws IOException, CanvasAPIException {
        when(okHttpClient.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(null);

        Assertions.assertThrows(
                CanvasAPIException.class,
                () -> canvasClientService.fetchFileUnderCourseAssignmentFolder(extensionUser, "fooFile")
        );
    }

    @Test
    public void testFetchFile_throwsCanvasAPIException() throws IOException, CanvasAPIException {
        when(okHttpClient.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(null);

        Assertions.assertThrows(
                CanvasAPIException.class,
                () -> canvasClientService.fetchFile(USER_ID, BEARER_TOKEN)
        );
    }

    @Test
    public void testFetchFoldersUnderStudent() throws IOException, CanvasAPIException {
        when(okHttpClient.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(response);

        JsonNode node = canvasClientService.fetchFoldersUnderStudent(USER_ID, BEARER_TOKEN);

        Assertions.assertEquals(
                node.get("id").asText(),
                "fooId"
        );
        Assertions.assertEquals(
                node.get("assignment").get("name").asText(),
                "fooAssignmentId"
        );
        Assertions.assertEquals(
                node.get("assignment").get("files_url").asText(),
                "http://fooUrl.foo"
        );
    }

    @Test
    public void testFetchFoldersUnderStudent_throwsCanvasAPIException() throws IOException {
        when(okHttpClient.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(null);

        Assertions.assertThrows(
                CanvasAPIException.class,
                () -> canvasClientService.fetchFoldersUnderStudent(USER_ID, BEARER_TOKEN)
        );
    }

    @Test
    public void testFetchFolders() throws CanvasAPIException, IOException {
        when(okHttpClient.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(response);

        JsonNode node = canvasClientService.fetchFolders(USER_ID, BEARER_TOKEN);

        Assertions.assertEquals(
                node.get("id").asText(),
                "fooId"
        );
        Assertions.assertEquals(
                node.get("assignment").get("name").asText(),
                "fooAssignmentId"
        );
        Assertions.assertEquals(
                node.get("assignment").get("files_url").asText(),
                "http://fooUrl.foo"
        );
    }

    @Test
    public void testFetchFolders_throwsCanvasAPIException() throws IOException {
        when(okHttpClient.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(null);

        Assertions.assertThrows(
                CanvasAPIException.class,
                () -> canvasClientService.fetchFolders(USER_ID, BEARER_TOKEN)
        );
    }

    @Test
    public void testFetchFilesUnderFolder() throws CanvasAPIException, IOException {
        when(okHttpClient.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(response);

        JsonNode node = canvasClientService.fetchFilesUnderFolder("fooFolder", BEARER_TOKEN);

        // TODO: sample for captor use, do after line coverage
//        verify(okHttpClient).newCall(requestCaptor.capture());
//        Request request = requestCaptor.getValue();

        Assertions.assertEquals(
                node.get("id").asText(),
                "fooId"
        );
        Assertions.assertEquals(
                node.get("assignment").get("name").asText(),
                "fooAssignmentId"
        );
        Assertions.assertEquals(
                node.get("assignment").get("files_url").asText(),
                "http://fooUrl.foo"
        );

    }

    @Test
    public void testFetchFilesUnderFolder_throwsCanvasAPIException() throws IOException {
        when(okHttpClient.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(null);

        Assertions.assertThrows(
                CanvasAPIException.class,
                () -> canvasClientService.fetchFilesUnderFolder("fooFolder", BEARER_TOKEN)
        );
    }

    @Test
    public void testFetchSubmissionFileBytes() throws IOException, CanvasAPIException {
        when(okHttpClient.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(attachmentResponse, filesResponse2, filesResponse3);


        Map<String, byte[]> submissionFileMap = canvasClientService.fetchStudentSubmissionFileBytes(extensionUser);

        Assertions.assertEquals(1, submissionFileMap.size());
        Assertions.assertEquals(93, submissionFileMap.get("fooFileName").length);
    }

    @Test
    public void testFetchSubmissionFileBytes_throwsCanvasAPIException() throws IOException {
        when(okHttpClient.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(null);

        Assertions.assertThrows(
                CanvasAPIException.class,
                () -> canvasClientService.fetchStudentSubmissionFileBytes(extensionUser)
        );
    }

    @Test
    public void testFetchCanvasSubmissionJson() throws IOException, CanvasAPIException {
        // Arrange
        when(okHttpClient.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(canvasSubmissionResponse);

        // Act
        JsonNode response = canvasClientService.fetchCanvasSubmissionJson(extensionUser);
        JsonNode attachments = response.get("attachments").get(0);

        // Assert
        Assertions.assertEquals(
                attachments.get("id").asText(),
                "fooId"
        );
        Assertions.assertEquals(
                attachments.get("filename").asText(),
                "fooFileName"
        );
        Assertions.assertEquals(
                attachments.get("preview_url").asText(),
                "foo/bar"
        );
    }

    @Test
    public void testFetchCanvasSubmissionJson_throwsCanvasAPIException() throws IOException {
        when(okHttpClient.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(null);

        Assertions.assertThrows(
                CanvasAPIException.class,
                () -> canvasClientService.fetchCanvasSubmissionJson(extensionUser)
        );
    }

    @Test
    public void testFetchStudentSubmission() throws IOException, CanvasAPIException {
        // Arrange
        when(okHttpClient.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(canvasSubmissionResponse, fetchFileResponse, fileUrlResponse);

        // Act
        Submission submission = canvasClientService.fetchStudentSubmission(extensionUser);

        // Assert
        Assertions.assertEquals("fooSubmissionId", submission.getSubmissionId());
        Assertions.assertEquals("fooAssignmentId", submission.getAssignmentId());
        Assertions.assertEquals("fooStudentId", submission.getStudentId());
        Assertions.assertArrayEquals(
                "{\"foo\": \"bar\"}]}".getBytes(),
                submission.getSubmissionFileBytes().get("fooFileName")
        );
        Assertions.assertNull(submission.getSubmissionFiles());
        Assertions.assertNull(submission.getSubmissionDirectory());
    }

    @Test
    public void testGetMyFilesFolderId() throws CanvasAPIException, IOException {
        when(okHttpClient.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(response);

        Assertions.assertEquals("12345", canvasClientService.getMyFilesFolderId(USER_ID, "fooAssignmentId", BEARER_TOKEN));
    }

    @Test
    public void testGetCanvasCodeFolderId() throws IOException, CanvasAPIException {
        when(okHttpClient.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(response);

        Assertions.assertEquals(
                "12345",
                canvasClientService.getCanvasCodeFolderId("fooFolder", "fooAssignmentId", BEARER_TOKEN)
        );
    }

    @Test
    public void testGetFileId() throws IOException, CanvasAPIException {
        when(okHttpClient.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(filesResponse1);

        Assertions.assertEquals(
                "12345",
                canvasClientService.getFileId("folderId", "fooAssignmentId", BEARER_TOKEN)
        );
    }

    @Test
    public void testGetAccessToken_returnsUnAuthroziedResponse() throws IOException, CanvasAPIException {
        when(okHttpClient.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(unAuthorizedAccessTokenResponse);
        Response response1 = canvasClientService.fetchAccessTokenResponse("testClientId", "testClientId", "testClientId", "testClientId");
        Assertions.assertEquals(
                401,
                response1.code()
        );
    }

    @Test
    public void testGetAccessToken_returnsSuccessResponse() throws IOException, CanvasAPIException {
        when(okHttpClient.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(successAccessTokenResponse);
        Response response1 = canvasClientService.fetchAccessTokenResponse("testClientId", "testClientId", "testClientId", "testClientId");
        Assertions.assertEquals(
                200,
                response1.code()
        );
    }

    @Test
    public void testGetAccessToken_throwsCanvasApiException() throws IOException, CanvasAPIException {
        when(okHttpClient.newCall(any())).thenReturn(call);
        when(call.execute()).thenThrow(new IOException());
        Assertions.assertThrows(
                CanvasAPIException.class,
                () -> canvasClientService.fetchAccessTokenResponse("testClientId", "testClientId", "testClientId", "testClientId")
        );
    }

    // Tests for helpers to get coverage
    @Test
    public void testGetFolderIdFromFoldersResponse_returnsNull() throws IOException {
        JsonNode node = canvasClientService.parseResponseToJsonNode(emptyResponse);
        Assertions.assertNull(canvasClientService.getFolderIdFromFoldersResponse(node, "fooFolder"));
    }

    @Test
    public void testGetFileIdFromFilesResponse_returnsNull() throws IOException {
        JsonNode node = canvasClientService.parseResponseToJsonNode(emptyResponse);
        Assertions.assertNull(canvasClientService.getFileIdFromFilesResponse(node, "fooFile"));
    }

    @Test
    public void testGetFilesRequestUrlFromAssignmentFolder_returnsNull() throws IOException {
        JsonNode node = canvasClientService.parseResponseToJsonNode(emptyResponse);
        Assertions.assertNull(canvasClientService.getFilesRequestUrlFromAssignmentFolder(node, "fooFolder"));
    }

}