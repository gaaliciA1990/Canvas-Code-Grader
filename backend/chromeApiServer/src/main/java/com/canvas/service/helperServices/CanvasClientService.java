package com.canvas.service.helperServices;

import com.canvas.exceptions.CanvasAPIException;
import com.canvas.service.models.ExtensionUser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * Service for accessing data from the Canvas API
 * <p>
 * All tests should be written in test/java/com.canvas/service
 * <p>
 * TODO: We should be referencing a user object here instead is possible
 */
@Service
public class CanvasClientService {

    // seattleU host: https://seattleu.instructure.com/api/v1
    public static final String CANVAS_URL = "https://canvas.instructure.com/api/v1";
    public static final String AUTH_HEADER = "Authorization";

    private final OkHttpClient okHttpClient;

    public CanvasClientService() {
        this.okHttpClient = new OkHttpClient();
    }

    /**
     * Calls the canvas api and fetches the user id based on the authorization token
     *
     * @param bearerToken authorization token from canvas
     * @return String of user id
     * @throws CanvasAPIException
     */
    public String fetchUserId(String bearerToken) throws CanvasAPIException {
        Request request = new Request.Builder()
                .url(CANVAS_URL + "/users/self")
                .get()
                .addHeader(AUTH_HEADER, bearerToken)
                .build();
        try {
            JsonNode resp = parseResponseToJsonNode(this.okHttpClient.newCall(request).execute());
            String userId = resp.get("id").asText();
            return userId;
        } catch (Exception e) {
            throw throwCanvasException(e);
        }
    }

    /**
     * Gets a file from a specific course assignment folder in Canvas
     *
     * @param user     Canvas user ID
     * @param fileName file name to fetch
     * @return returns a byte array of the file
     * @throws CanvasAPIException
     */
    public byte[] fetchFileUnderCourseAssignmentFolder(ExtensionUser user, String fileName) throws CanvasAPIException {
        try {
            JsonNode foldersResp = fetchFoldersUnderCourse(user.getCourseId(), user.getBearerToken());
            // assignmentId is the folder name
            String filesRequestUrl = getFilesRequestUrlFromAssignmentFolder(foldersResp, user.getAssignmentId());

            Request filesRequest = new Request.Builder()
                    .url(filesRequestUrl)
                    .get()
                    .addHeader(AUTH_HEADER, user.getBearerToken())
                    .build();

            JsonNode filesResponse = parseResponseToJsonNode(this.okHttpClient.newCall(filesRequest).execute());
            String fileId = getFileIdFromFilesResponse(filesResponse, fileName + ".dms");
            return fetchFile(fileId, user.getBearerToken());
        } catch (Exception e) {
            throw throwCanvasException(e);
        }
    }

    /**
     * Get a file from Canvas
     *
     * @param fileId      file to fetch from Canvas
     * @param bearerToken authorization token
     * @return returns a byte array of the file
     * @throws CanvasAPIException
     */
    public byte[] fetchFile(String fileId, String bearerToken) throws CanvasAPIException {
        Request request = new Request.Builder()
                .url(CANVAS_URL + "/files/" + fileId)
                .get()
                .addHeader(AUTH_HEADER, bearerToken)
                .build();
        try {
            JsonNode resp = parseResponseToJsonNode(this.okHttpClient.newCall(request).execute());
            String url = resp.get("url").asText();

            Request fileRequest = new Request.Builder().url(url).get().build();
            try (Response fileResp = this.okHttpClient.newCall(fileRequest).execute()) {
                return Objects.requireNonNull(fileResp.body()).bytes();
            }
        } catch (Exception e) {
            throw throwCanvasException(e);
        }
    }

    /**
     * Gets all the folders for a given course id
     *
     * @param courseId    Canvas course id
     * @param bearerToken authorization token
     * @return JsonNode
     * @throws CanvasAPIException
     */
    public JsonNode fetchFoldersUnderCourse(String courseId, String bearerToken) throws CanvasAPIException {
        Request request = new Request.Builder()
                .url(CANVAS_URL + "/courses/" + courseId + "/folders")
                .get()
                .addHeader(AUTH_HEADER, bearerToken)
                .build();
        try {
            return parseResponseToJsonNode(this.okHttpClient.newCall(request).execute());
        } catch (Exception e) {
            throw throwCanvasException(e);
        }
    }

    /**
     * Gets all the folder associated with a student
     *
     * @param userId      Canvas user id
     * @param bearerToken Authorization Token
     * @return JsonNode
     * @throws CanvasAPIException
     */
    public JsonNode fetchFoldersUnderStudent(String userId, String bearerToken) throws CanvasAPIException {
        Request request = new Request.Builder()
                .url(CANVAS_URL + "/users/" + userId + "/folders/by_path")
                .get()
                .addHeader(AUTH_HEADER, bearerToken)
                .build();
        try {
            return parseResponseToJsonNode(this.okHttpClient.newCall(request).execute());
        } catch (Exception e) {
            throw throwCanvasException(e);
        }
    }

    /**
     * Gets all folders for a specific user
     *
     * @param folderId    folder id to return
     * @param bearerToken Authorization Token
     * @return JsonNode
     * @throws CanvasAPIException
     */
    public JsonNode fetchFolders(String folderId, String bearerToken) throws CanvasAPIException {
        Request request = new Request.Builder()
                .url(CANVAS_URL + "/folders/" + folderId + "/folders")
                .get()
                .addHeader(AUTH_HEADER, bearerToken)
                .build();
        try {
            return parseResponseToJsonNode(this.okHttpClient.newCall(request).execute());
        } catch (Exception e) {
            throw throwCanvasException(e);
        }
    }

    /**
     * Gets all files under a given folder
     *
     * @param folderId    folder to extract files from
     * @param bearerToken Authorization token
     * @return JsonNode
     * @throws CanvasAPIException
     */
    public JsonNode fetchFilesUnderFolder(String folderId, String bearerToken) throws CanvasAPIException {
        Request request = new Request.Builder()
                .url(CANVAS_URL + "/folders/" + folderId + "/files")
                .get()
                .addHeader(AUTH_HEADER, bearerToken)
                .build();
        try {
            return parseResponseToJsonNode(this.okHttpClient.newCall(request).execute());
        } catch (Exception e) {
            throw throwCanvasException(e);
        }
    }

    /**
     * TODO: Add comments
     *
     * @param user
     * @return
     * @throws CanvasAPIException
     */
    public Map<String, byte[]> fetchSubmissionFilesFromStudent(ExtensionUser user) throws CanvasAPIException {
        Map<String, byte[]> submissionFilesBytes = new HashMap<>();

        Request request = new Request.Builder()
                .url(CANVAS_URL + "/courses/" + user.getCourseId() + "/assignments/" + user.getAssignmentId() + "/submissions/" + user.getStudentId())
                .get()
                .addHeader(AUTH_HEADER, user.getBearerToken())
                .build();
        try {
            JsonNode submissionResp = parseResponseToJsonNode(this.okHttpClient.newCall(request).execute());
            JsonNode filesAttachment = submissionResp.get("attachments");
            for (JsonNode fileJson : filesAttachment) {
                String fileId = fileJson.get("id").asText();
                byte[] fileBytes = fetchFile(fileId, user.getBearerToken());
                String fileName = fileJson.get("filename").asText();
                submissionFilesBytes.put(fileName, fileBytes);
            }

            return submissionFilesBytes;
        } catch (Exception e) {
            throw throwCanvasException(e);
        }
    }

    /**
     * Gets an assignment submission from a file and saves it TODO: elaborate on what this is doing?
     *
     * @param fileName    file name to search the submission under
     * @param bearerToken Authorization token
     * @throws CanvasAPIException
     */
    public void fetchSubmissionFromMyFilesAndSave(String fileName, String bearerToken) throws CanvasAPIException {
        try {
            String userId = this.fetchUserId(bearerToken);
            String myFilesId = this.getMyFilesFolderId(userId, "my files", bearerToken);
            String canvasCodeFolderId = this.getCanvasCodeFolderId(myFilesId, "CanvasCode", bearerToken);
            String submissionFileId = this.getFileId(canvasCodeFolderId, fileName, bearerToken);
            byte[] fileBytes = this.fetchFile(submissionFileId, bearerToken);

            FileService fs = new FileService();
            // default stores file in /tmp
            fs.writeFileFromBytes(fileName, fileBytes, "1");
        } catch (Exception e) {
            throw throwCanvasException(e);
        }
    }

    /**
     * Helper method for throwing exceptions associated with Canvas connection issues
     *
     * @return CanvasAPI exception
     */
    private CanvasAPIException throwCanvasException(Exception e){
        String errMsg = "Failure to connect with Canvas API";
        System.out.println(errMsg);  // TODO: use logger for printing to console
        return new CanvasAPIException(errMsg, e);
    }

    /**
     * Parses a response to a JSON node Todo: explain more about what this is doing
     *
     * @param response
     * @return
     * @throws IOException
     */
    private JsonNode parseResponseToJsonNode(Response response) throws IOException {
        String r = response.body().string();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(r);
    }

    /**
     * Todo: Add comments explaining this helpder method
     *
     * @param userId
     * @param folderName
     * @param accessToken
     * @return
     * @throws CanvasAPIException
     */
    private String getMyFilesFolderId(String userId, String folderName, String accessToken) throws CanvasAPIException {
        JsonNode response = fetchFoldersUnderStudent(userId, accessToken);
        return getFolderIdFromFoldersResponse(response, folderName);
    }

    /**
     * Help function to extract the Canvas folder id TODO: explain more about where this is coming from
     *
     * @param folderId
     * @param folderName
     * @param accessToken
     * @return
     * @throws IOException
     */
    private String getCanvasCodeFolderId(String folderId, String folderName, String accessToken) throws CanvasAPIException {
        JsonNode response = fetchFolders(folderId, accessToken);
        return getFolderIdFromFoldersResponse(response, folderName);
    }

    /**
     * Helper method to get the file id TODO: explain more about what this is doing
     *
     * @param folderId
     * @param fileName
     * @param bearerToken
     * @return
     * @throws CanvasAPIException
     */
    private String getFileId(String folderId, String fileName, String bearerToken) throws CanvasAPIException {
        JsonNode response = fetchFilesUnderFolder(folderId, bearerToken);
        return getFileIdFromFilesResponse(response, fileName);
    }

    /**
     * Helper method for something? TODO: explain more about what this is doing
     *
     * @param response
     * @param folderName
     * @return
     * @throws IOException
     */
    private String getFolderIdFromFoldersResponse(JsonNode response, String folderName) {
        for (Iterator<JsonNode> it = response.elements(); it.hasNext(); ) {
            JsonNode folder = it.next();
            if (folder.get("name").asText().equals(folderName)) {
                return folder.get("id").toString();
            }
        }
        return null;
    }

    /**
     * Help method for TODO: explain more about what this is doing
     *
     * @param response
     * @param fileName
     * @return
     * @throws IOException
     */
    private String getFileIdFromFilesResponse(JsonNode response, String fileName) {
        for (Iterator<JsonNode> it = response.elements(); it.hasNext(); ) {
            JsonNode folder = it.next();
            if (folder.get("filename").asText().equals(fileName)) {
                return folder.get("id").toString();
            }
        }
        return null;
    }

    /**
     * Helper method for extracting something from the url TODO: explain more about what this is doing
     *
     * @param response
     * @param folderName
     * @return
     * @throws IOException
     */
    private String getFilesRequestUrlFromAssignmentFolder(JsonNode response, String folderName) {
        for (Iterator<JsonNode> it = response.elements(); it.hasNext(); ) {
            JsonNode folder = it.next();
            if (folder.get("name").asText().equals(folderName)) {
                return folder.get("files_url").asText();
            }
        }
        return null;
    }
}
