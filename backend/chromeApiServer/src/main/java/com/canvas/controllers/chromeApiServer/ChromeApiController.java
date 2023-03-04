package com.canvas.controllers.chromeApiServer;

import com.canvas.exceptions.CanvasAPIException;
import com.canvas.exceptions.IncorrectRequestParamsException;
import com.canvas.exceptions.UserNotAuthorizedException;
import com.canvas.service.EvaluationService;
import com.canvas.service.helperServices.AESCryptoService;
import com.canvas.service.helperServices.OAuthService;
import com.canvas.service.SubmissionDirectoryService;
import com.canvas.service.models.CommandOutput;
import com.canvas.service.helperServices.CanvasClientService;
import com.canvas.service.models.ExtensionUser;
import com.canvas.service.models.UserType;
import com.canvas.service.models.submission.Deletion;
import com.canvas.service.models.submission.Submission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * API Controller for the Chrome Extension handling all the GET and POST requests.
 */
@RestController
@CrossOrigin
public class ChromeApiController {
    private final EvaluationService evaluation;
    private final CanvasClientService canvasClientService;
    private final SubmissionDirectoryService submissionDirectoryService;
    private final OAuthService oauthService;

    @Autowired
    Environment env;

    private final AESCryptoService aesCryptoService;

    // Logger object
    private static final Logger logger = LoggerFactory.getLogger(ChromeApiController.class);


    @Autowired
    public ChromeApiController(
            EvaluationService studentEval,
            CanvasClientService canvasClientService,
            SubmissionDirectoryService submissionDirectoryService,
            AESCryptoService aesCryptoService,
            Environment env) {
        this.evaluation = studentEval;
        this.canvasClientService = canvasClientService;
        this.oauthService = new OAuthService(env, canvasClientService);
        this.submissionDirectoryService = submissionDirectoryService;
        this.aesCryptoService = aesCryptoService;
    }

    /**
     * API for retrieving the student submission files and generating the student submission directory.
     *
     * @param bearerToken authorization token
     * @param assignmentId Canvas assignment ID
     * @param courseId Canvas course ID
     * @param studentId Canvas student ID
     * @param userType User Type Enum indicating who is using the extension
     * @return student submission JSON
     * @throws CanvasAPIException
     * @throws IncorrectRequestParamsException
     * @throws UserNotAuthorizedException
     */
    @GetMapping(
            value = "/submission/courses/{courseId}/assignments/{assignmentId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Submission> retrieveStudentSubmissionFiles(
            HttpServletRequest request,
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable("assignmentId") String assignmentId,
            @PathVariable("courseId") String courseId,
            @RequestParam("studentId") String studentId,
            @RequestParam("userType") UserType userType
    ) throws CanvasAPIException, IncorrectRequestParamsException, UserNotAuthorizedException {
        validateGraderRequest(bearerToken, assignmentId, courseId, studentId, userType);

        String decryptedBearerToken = request.getHeader("Authorization");

        String userId = canvasClientService.fetchUserId(decryptedBearerToken);
        ExtensionUser graderUser = new ExtensionUser(decryptedBearerToken, userId, courseId, assignmentId, studentId, userType);
        return submissionDirectoryService.generateSubmissionDirectory(graderUser);
    }

    @DeleteMapping(
            value = "/submission/courses/{courseId}/assignments/{assignmentId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Deletion> deleteStudentSubmissionFiles(
            HttpServletRequest request,
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable("assignmentId") String assignmentId,
            @PathVariable("courseId") String courseId,
            @RequestParam("studentId") String studentId,
            @RequestParam("userType") UserType userType
    ) throws UserNotAuthorizedException, IncorrectRequestParamsException, CanvasAPIException {
        validateGraderRequest(bearerToken, assignmentId, courseId, studentId, userType);

        String decryptedBearerToken = request.getHeader("Authorization");

        String userId = canvasClientService.fetchUserId(decryptedBearerToken);
        ExtensionUser graderUser = new ExtensionUser(decryptedBearerToken, userId, courseId, assignmentId, studentId, userType);
        return submissionDirectoryService.deleteSubmissionDirectory(graderUser);
    }

    /**
     * Post request to start the evaluation service.
     * Depending on type of user, the correct evaluation path is called.
     *
     * @param bearerToken  authorization token from Canvas API
     * @param files        file(s) to be evaluated
     * @param assignmentId Canvas assignment id
     * @param courseId     Canvas course id
     * @param type         User Type Enum indicating who is using the extension
     * @return Response CommandOutput
     */
    @PostMapping(
            value = "/evaluate",
            produces = {"application/json"}, //This method should only start the program and return a success response
            consumes = {"multipart/form-data"}
    )
    public ResponseEntity<CommandOutput> initiateStudentCodeEvaluation(
            HttpServletRequest request,
            @RequestHeader("Authorization") String bearerToken,
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("assignmentId") String assignmentId,
            @RequestParam("courseId") String courseId,
            @RequestParam("userType") UserType type
    ) throws UserNotAuthorizedException, IncorrectRequestParamsException, CanvasAPIException, IOException {
        // Check request params are correct, not null or empty.
        // TODO: What do we consider incorrect? Define further
        if (bearerToken == null || assignmentId == null || courseId == null || files == null || type == null) {
            String warnMsg = "Incorrect request parameters received. Check the parameters meet the requirements";
            logger.warn(warnMsg);
            throw new IncorrectRequestParamsException(warnMsg);
        }

        if (bearerToken.isEmpty() || files.length == 0 || assignmentId.isEmpty() || courseId.isEmpty()) {
            String warnMsg = "Incorrect request parameters received. Check the parameters meet the requirements";
            logger.warn(warnMsg);
            throw new IncorrectRequestParamsException(warnMsg);
        }

        String decryptedBearerToken = request.getHeader("Authorization");

        // check userType isn't Unauthorized or Grader
        if (type == UserType.UNAUTHORIZED || type == UserType.GRADER) {
            String errorMessage = String.format("user type [%s] does not match expected [%s]", type, UserType.STUDENT);
            logger.error(errorMessage);
            throw new UserNotAuthorizedException(errorMessage);
        }

        // Get the user id from Canvas API
        String userId = canvasClientService.fetchUserId(decryptedBearerToken);

        // Create a user object with params, studentId is null
        ExtensionUser user = new ExtensionUser(decryptedBearerToken, userId, courseId, assignmentId, null, type);

        // TODO: check if submission includes makefile
        return evaluation.compileStudentCodeFile(user, files);
    }

    /**
     * Get request to fetch access token from canvas.
     *
     * @param code code returned by canvas after login
     * @return Redirects to loginSuccess/loginFailure route based on access token response from canvas
     * @throws IncorrectRequestParamsException
     */
    @GetMapping(
            value = "/oauth2Response"
    )
    public ResponseEntity<String> oAuth2Response (
            @RequestParam(value = "code", required = true) String code) {
        try {
            String accessToken = this.oauthService.fetchAccessTokenResponse(code,"http://csrh51.cslab.seattleu.edu:8080/oauth2Response");
            HttpHeaders headers = new HttpHeaders();
            String encryptedAccessToken = aesCryptoService.encrypt(accessToken, "testSecretKey");
            headers.add("Location", "/loginSuccess?access_token=" + encryptedAccessToken);
            String decryptedAccessToken = aesCryptoService.decrypt(encryptedAccessToken, "testSecretKey");
            return new ResponseEntity<String>("Login Success",headers, HttpStatus.FOUND);
        } catch (Exception e) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", "/loginFail");
            return new ResponseEntity<String>(e.getMessage(),headers,HttpStatus.FOUND);
        }
    }

    /**
     * Get route upon login success
     * @return Status OK / 200
     */
    @GetMapping(value = "/loginSuccess")
    public ResponseEntity<String> loginSuccess() {
        return new ResponseEntity<String>("Login success",HttpStatus.OK);
    }


    /**
     * Get route upon login fail
     * @return unauthorized / 401
     */
    @GetMapping(value = "/loginFail")
    public ResponseEntity<String> loginFail() {
        return new ResponseEntity<String>("Login Failed",HttpStatus.UNAUTHORIZED);
    }

    /**
     * Helper method for validating a request.
     *
     * @param bearerToken authorization token
     * @param assignmentId Canvas assignment ID
     * @param courseId Canvas course ID
     * @param studentId Canvas student ID
     * @param userType User Type Enum indicating who is using the extension
     * @throws IncorrectRequestParamsException
     * @throws UserNotAuthorizedException
     */
    private void validateGraderRequest(
            String bearerToken,
            String assignmentId,
            String courseId,
            String studentId,
            UserType userType
    ) throws IncorrectRequestParamsException, UserNotAuthorizedException {
        // Check request params are correct, not null or empty.
        // TODO: What do we consider incorrect? Define further
        if (bearerToken == null || assignmentId == null || courseId == null || studentId == null || userType == null) {
            String warnMsg = "Incorrect request parameters received. Check the parameters meet the requirements";
            logger.warn(warnMsg);
            throw new IncorrectRequestParamsException(warnMsg);
        }

        if (bearerToken.isEmpty() || assignmentId.isEmpty() || courseId.isEmpty() || studentId.isEmpty()) {
            String warnMsg = "Incorrect request parameters received. Check the parameters meet the requirements";
            logger.warn(warnMsg);
            throw new IncorrectRequestParamsException(warnMsg);
        }

        // check userType isn't Unauthorized or Student
        if (userType == UserType.UNAUTHORIZED || userType == UserType.STUDENT) {
            String errorMessage = String.format("user type [%s] does not match expected [%s]", userType, UserType.GRADER);
            logger.error(errorMessage);
            throw new UserNotAuthorizedException(errorMessage);
        }
    }
}