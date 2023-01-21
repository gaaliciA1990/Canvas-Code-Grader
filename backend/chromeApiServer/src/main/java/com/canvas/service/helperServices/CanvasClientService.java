package com.canvas.service.helperServices;

import com.canvas.controllers.chromeApiServer.ChromeApiController;
import com.canvas.exceptions.CanvasAPIException;
import com.canvas.service.models.ExtensionUser;
import com.canvas.service.models.submission.Submission;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    // Logger object
    private static final Logger logger = LoggerFactory.getLogger(CanvasClientService.class);


    /**
     * Constructor with not arguments
     */
    public CanvasClientService() {
        this.okHttpClient = new OkHttpClient();
    }

    protected CanvasClientService(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    /**
     * Calls the canvas api and fetches the user id based on the authorization token
     *
     * @param bearerToken authorization token from canvas
     * @return String of user id
     * @throws CanvasAPIException error message thrown if connection to canvas fails
     */
    public String fetchUserId(String bearerToken) throws CanvasAPIException {
        Request request = new Request.Builder()
                .url(CANVAS_URL + "/users/self")
                .get()
                .addHeader(AUTH_HEADER, bearerToken)
                .build();
        try {
            JsonNode resp = parseResponseToJsonNode(this.okHttpClient.newCall(request).execute());
            return resp.get("id").asText();
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
     * @throws CanvasAPIException error message thrown if connection to canvas fails
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
     * @throws CanvasAPIException error message thrown if connection to canvas fails
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
     * @throws CanvasAPIException error message thrown if connection to canvas fails
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
     * Gets all the folders associated with a student
     *
     * @param userId      Canvas user id
     * @param bearerToken Authorization Token
     * @return JsonNode
     * @throws CanvasAPIException error message thrown if connection to canvas fails
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
     * @throws CanvasAPIException error message thrown if connection to canvas fails
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
     * @throws CanvasAPIException error message thrown if connection to canvas fails
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
     * Fetches the Canvas submission JSON.
     *
     * @param user Extension user
     * @return Canvas submission JSON
     * @throws CanvasAPIException
     */
    public JsonNode fetchCanvasSubmissionJson(ExtensionUser user) throws CanvasAPIException {
        Request request = new Request.Builder()
                .url(CANVAS_URL + "/courses/" + user.getCourseId() + "/assignments/" + user.getAssignmentId() + "/submissions/" + user.getStudentId())
                .get()
                .addHeader(AUTH_HEADER, user.getBearerToken())
                .build();
        try {
            return parseResponseToJsonNode(this.okHttpClient.newCall(request).execute());
        } catch (Exception e) {
            throw throwCanvasException(e);
        }
    }

    /**
     * Creates the student submission model using the Canvas submission JSON.
     *
     * @param user Extension user
     * @return Submission model
     * @throws CanvasAPIException
     */
    public Submission fetchStudentSubmission(ExtensionUser user) throws CanvasAPIException {
        JsonNode submissionResp = fetchCanvasSubmissionJson(user);
        Map<String, byte[]> submissionFilesBytes = generateSubmissionFileBytes(submissionResp, user.getBearerToken());

        String submissionId = submissionResp.get("id").asText();

        return Submission.builder()
                .submissionId(submissionId)
                .studentId(user.getStudentId())
                .assignmentId(user.getAssignmentId())
                .submissionFileBytes(submissionFilesBytes)
                .build();
    }

    /**
     * Fetch the submission file byte map.
     *
     * @param user Extension user
     * @return submission file byte map
     * @throws CanvasAPIException
     */
    public Map<String, byte[]> fetchStudentSubmissionFileBytes(ExtensionUser user) throws CanvasAPIException {
        JsonNode submissionResp = fetchCanvasSubmissionJson(user);
        return generateSubmissionFileBytes(submissionResp, user.getBearerToken());
    }

    /**
     * Gets an assignment submission from a file and saves it.
     * BOILER PLATE CODE FOR INTERACTING WITH CANVAS.
     *
     * @param fileName    file name to search the submission under
     * @param bearerToken Authorization token
     * @throws CanvasAPIException error message thrown if connection to canvas fails
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

    private Map<String, byte[]> generateSubmissionFileBytes(JsonNode submissionResp, String bearerToken) throws CanvasAPIException {
        Map<String, byte[]> submissionFilesBytes = new HashMap<>();
        JsonNode filesAttachment = submissionResp.get("attachments");
        for (JsonNode fileJson : filesAttachment) {
            String fileId = fileJson.get("id").asText();
            byte[] fileBytes = fetchFile(fileId, bearerToken);
            String fileName = fileJson.get("filename").asText();
            submissionFilesBytes.put(fileName, fileBytes);
        }

        return submissionFilesBytes;
    }

    /**
     * Helper method for throwing exception associated with Canvas connection issues
     *
     * @return CanvasAPI exception
     */
    private CanvasAPIException throwCanvasException(Exception e) {
        String errMsg = "Failure to connect with Canvas API";
        logger.error(errMsg);
        return new CanvasAPIException(errMsg, e);
    }

    /**
     * Parses a response to a JSON node
     *
     * @param response okhttp3 response type
     * @return JsonNode
     * @throws IOException error message thrown
     */
    protected JsonNode parseResponseToJsonNode(Response response) throws IOException {
        String r = response.body().string();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(r);
    }

    /**
     * Helper method for retrieving folders for a given Canvas user
     *
     * @param userId      Canvas userId
     * @param folderName  name of folder returned
     * @param bearerToken authorization token
     * @return a string of response and folder name
     * @throws CanvasAPIException error message thrown if connection to canvas fails
     */
    protected String getMyFilesFolderId(String userId, String folderName, String bearerToken) throws CanvasAPIException {
        JsonNode response = fetchFoldersUnderStudent(userId, bearerToken);
        return getFolderIdFromFoldersResponse(response, folderName);
    }

    /**
     * Helper function to extract the Canvas folder id
     *
     * @param folderId folder id to get
     * @param folderName name of folder to search
     * @param bearerToken authorization token
     * @return String of JsonNode respons and folder name
     * @throws CanvasAPIException error message thrown if connection to canvas fails
     */
    protected String getCanvasCodeFolderId(String folderId, String folderName, String bearerToken) throws CanvasAPIException {
        JsonNode response = fetchFolders(folderId, bearerToken);
        return getFolderIdFromFoldersResponse(response, folderName);
    }

    /**
     * Helper method to get the file id
     *
     * @param folderId  folder id where file is contain
     * @param fileName name of file to look for
     * @param bearerToken authorization token
     * @return String of JsonNode response and filename
     * @throws CanvasAPIException error message thrown if connection to canvas fails
     */
    protected String getFileId(String folderId, String fileName, String bearerToken) throws CanvasAPIException {
        JsonNode response = fetchFilesUnderFolder(folderId, bearerToken);
        return getFileIdFromFilesResponse(response, fileName);
    }

    /**
     * Helper method for getting a folder Id based on JsonNode response
     *
     * @param response   JsonNode response type
     * @param folderName name of folder to return
     * @return String of folder Id
     */
    protected String getFolderIdFromFoldersResponse(JsonNode response, String folderName) {
        for (Iterator<JsonNode> it = response.elements(); it.hasNext(); ) {
            JsonNode folder = it.next();
            JsonNode name = folder.get("name");
            if (name != null && folder.get("name").asText().equals(folderName)) {
                return folder.get("id").toString();
            }
        }
        return null;
    }

    /**
     * Help method for getting file name based on JsonNode response
     *
     * @param response JsonNode respone type
     * @param fileName name of file to retrieve
     * @return string of file id
     */
    protected String getFileIdFromFilesResponse(JsonNode response, String fileName) {
        for (Iterator<JsonNode> it = response.elements(); it.hasNext(); ) {
            JsonNode folder = it.next();
            JsonNode name = folder.get("filename");
            if (name != null && folder.get("filename").asText().equals(fileName)) {
                return folder.get("id").toString();
            }
        }
        return null;
    }

    /**
     * Helper method for extracting foldername from the url
     *
     * @param response   JsonNode response type
     * @param folderName name of folder to return
     * @return String of file url for the folder search
     */
    protected String getFilesRequestUrlFromAssignmentFolder(JsonNode response, String folderName) {
        for (Iterator<JsonNode> it = response.elements(); it.hasNext(); ) {
            JsonNode folder = it.next();
            JsonNode name = folder.get("name");
            if (name != null && folder.get("name").asText().equals(folderName)) {
                return folder.get("files_url").asText();
            }
        }
        return null;
    }
}
