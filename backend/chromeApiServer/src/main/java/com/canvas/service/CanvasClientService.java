package com.canvas.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public class CanvasClientService {

    // seattleU host: https://seattleu.instructure.com/api/v1
    public static final String CANVAS_URL = "https://canvas.instructure.com/api/v1";
    public static final String AUTH_HEADER = "Authorization";

    private final OkHttpClient okHttpClient;
    private final String accessToken;
    public CanvasClientService(String token) {
        this.okHttpClient = new OkHttpClient();
        this.accessToken = token;
    }

    private JsonNode parseResponseToJsonNode(Response response) throws IOException {
        String r =  response.body().string();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(r);
    }
    private String getMyFilesFolderId(String userId, String folderName) throws IOException{
        JsonNode response = fetchFoldersUnderStudent(userId);
        return getFolderIdFromFoldersResponse(response, folderName);
    }

    private String getCanvasCodeFolderId(String folderId, String folderName) throws IOException{
        JsonNode response = fetchFolders(folderId);
        return getFolderIdFromFoldersResponse(response, folderName);
    }

    private String getFileId(String folderId, String fileName) throws IOException {
        JsonNode response = fetchFilesUnderFolder(folderId);
        return getFileIdFromFilesResponse(response, fileName);
    }

    private String getFolderIdFromFoldersResponse(JsonNode response, String folderName) throws IOException{
        for (Iterator<JsonNode> it = response.elements(); it.hasNext(); ) {
            JsonNode folder = it.next();
            if (folder.get("name").asText().equals(folderName)) {
                return folder.get("id").toString();
            }
        }
        return null;
    }
    private String getFileIdFromFilesResponse(JsonNode response, String fileName) throws IOException{
        for (Iterator<JsonNode> it = response.elements(); it.hasNext(); ) {
            JsonNode folder = it.next();
            if (folder.get("filename").asText().equals(fileName)) {
                return folder.get("id").toString();
            }
        }
        return null;
    }

    private String getFilesRequestUrlFromAssignmentFolder(JsonNode response, String folderName) throws IOException{
        for (Iterator<JsonNode> it = response.elements(); it.hasNext(); ) {
            JsonNode folder = it.next();
            if (folder.get("name").asText().equals(folderName)) {
                return folder.get("files_url").asText();
            }
        }
        return null;
    }

    public byte[] fetchFile(String fileId) throws IOException {
        Request request = new Request.Builder()
                .url(CANVAS_URL + "/files/" + fileId)
                .get()
                .addHeader(AUTH_HEADER, this.accessToken)
                .build();
        JsonNode resp = parseResponseToJsonNode(this.okHttpClient.newCall(request).execute());
        String url = resp.get("url").asText();

        Request fileRequest = new Request.Builder().url(url).get().build();
        Response fileResp = this.okHttpClient.newCall(fileRequest).execute();
        return Objects.requireNonNull(fileResp.body()).bytes();
    }

    public byte[] fetchFileUnderCourseAssignmentFolder(String courseId, String assignmentId, String fileName) throws IOException {
        JsonNode foldersResp = fetchFoldersUnderCourse(courseId);
        // assignmentId is the folder name
        String filesRequestUrl = getFilesRequestUrlFromAssignmentFolder(foldersResp, assignmentId);

        Request filesRequest = new Request.Builder()
                .url(filesRequestUrl)
                .get()
                .addHeader(AUTH_HEADER, this.accessToken)
                .build();

        JsonNode filesResponse = parseResponseToJsonNode(this.okHttpClient.newCall(filesRequest).execute());
        String fileId = getFileIdFromFilesResponse(filesResponse, fileName + ".dms");
        return fetchFile(fileId);
    }

    public String fetchUserId() throws IOException {
        Request request = new Request.Builder()
                .url(CANVAS_URL + "/users/self")
                .get()
                .addHeader(AUTH_HEADER, this.accessToken)
                .build();
        JsonNode resp = parseResponseToJsonNode(this.okHttpClient.newCall(request).execute());
        String userId = resp.get("id").asText();
        return userId;

    }

    public JsonNode fetchFoldersUnderCourse(String courseId) throws IOException {
        Request request = new Request.Builder()
                .url(CANVAS_URL + "/courses/" + courseId + "/folders")
                .get()
                .addHeader(AUTH_HEADER, this.accessToken)
                .build();
        return parseResponseToJsonNode(this.okHttpClient.newCall(request).execute());
    }

    public JsonNode fetchFoldersUnderStudent(String userId) throws IOException {
        Request request = new Request.Builder()
                .url(CANVAS_URL + "/users/" + userId + "/folders/by_path")
                .get()
                .addHeader(AUTH_HEADER, this.accessToken)
                .build();
        return parseResponseToJsonNode(this.okHttpClient.newCall(request).execute());
    }
    public JsonNode fetchFolders(String folderId) throws IOException {
        Request request = new Request.Builder()
                .url(CANVAS_URL + "/folders/" + folderId + "/folders")
                .get()
                .addHeader(AUTH_HEADER, this.accessToken)
                .build();
        return parseResponseToJsonNode(this.okHttpClient.newCall(request).execute());
    }
    public JsonNode fetchFilesUnderFolder(String folderId) throws IOException {
        Request request = new Request.Builder()
                .url(CANVAS_URL + "/folders/" + folderId + "/files")
                .get()
                .addHeader(AUTH_HEADER, this.accessToken)
                .build();
        return parseResponseToJsonNode(this.okHttpClient.newCall(request).execute());
    }

    public void fetchSubmissionFromMyFilesAndSave(String fileName) throws IOException {
        String userId = this.fetchUserId();
        String myFilesId = this.getMyFilesFolderId(userId, "my files");
        String canvasCodeFolderId = this.getCanvasCodeFolderId(myFilesId, "CanvasCode");
        String submissionFileId = this.getFileId(canvasCodeFolderId, fileName);
        byte[] fileBytes = this.fetchFile(submissionFileId);

        FileService fs = new FileService("1");
        // default stores file in /tmp
        fs.writeFileFromBytes(fileName,fileBytes);
    }

    //Test
    public static void main(String[] args) throws IOException {
        CanvasClientService canvasClientServices = new CanvasClientService("Bearer");
        String userId = canvasClientServices.fetchUserId();
        String myFilesId = canvasClientServices.getMyFilesFolderId(userId, "my files");
        // This will change to Submissions folder
        String canvasCodeFolderId = canvasClientServices.getCanvasCodeFolderId(myFilesId, "CanvasCode");
        String submissionFileId = canvasClientServices.getFileId(canvasCodeFolderId, "sample.cpp");
        byte[] fileBytes = canvasClientServices.fetchFile(submissionFileId);

        FileService fs = new FileService("1");
        fs.writeFileFromBytes("sample1.cpp",fileBytes);
    }
}
