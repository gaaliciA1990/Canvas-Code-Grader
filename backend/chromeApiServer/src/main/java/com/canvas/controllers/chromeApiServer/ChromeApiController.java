package com.canvas.controllers.chromeApiServer;

import com.canvas.exceptions.CanvasAPIException;
import com.canvas.exceptions.IncorrectRequestParamsException;
import com.canvas.exceptions.UserNotAuthorizedException;
import com.canvas.service.EvaluationService;
import com.canvas.service.SubmissionDirectoryService;
import com.canvas.service.models.CommandOutput;
import com.canvas.service.helperServices.CanvasClientService;
import com.canvas.service.models.ExtensionUser;
import com.canvas.service.models.UserType;
import com.canvas.service.models.submission.Submission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

/**
 * API Controller for the Chrome Extension handling all the GET and POST requests.
 */
@RestController
@CrossOrigin
public class ChromeApiController {
    private final EvaluationService evaluation;
    private final CanvasClientService canvasClientService;
    private final SubmissionDirectoryService submissionDirectoryService;

    // Logger object
    private static final Logger logger = LoggerFactory.getLogger(ChromeApiController.class);


    @Autowired
    public ChromeApiController(
            EvaluationService studentEval,
            CanvasClientService canvasClientService,
            SubmissionDirectoryService submissionDirectoryService
    ) {
        this.evaluation = studentEval;
        this.canvasClientService = canvasClientService;
        this.submissionDirectoryService = submissionDirectoryService;
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
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable("assignmentId") String assignmentId,
            @PathVariable("courseId") String courseId,
            @RequestParam("studentId") String studentId,
            @RequestParam("userType") UserType userType
    ) throws CanvasAPIException, IncorrectRequestParamsException, UserNotAuthorizedException {
        validateGraderRequest(bearerToken, assignmentId, courseId, studentId, userType);

        String userId = canvasClientService.fetchUserId(bearerToken);
        ExtensionUser graderUser = new ExtensionUser(bearerToken, userId, courseId, assignmentId, studentId, userType);
        return submissionDirectoryService.generateSubmissionDirectory(graderUser);
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
            @RequestHeader("Authorization") String bearerToken,
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("assignmentId") String assignmentId,
            @RequestParam("courseId") String courseId,
            @RequestParam("userType") UserType type
    ) throws UserNotAuthorizedException, IncorrectRequestParamsException, CanvasAPIException {
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
        // check userType isn't Unauthorized or Grader
        if (type == UserType.UNAUTHORIZED || type == UserType.GRADER) {
            String errorMessage = String.format("user type [%s] does not match expected [%s]", type, UserType.STUDENT);
            logger.error(errorMessage);
            throw new UserNotAuthorizedException(errorMessage);
        }

        // Get the user id from Canvas API
        String userId = canvasClientService.fetchUserId(bearerToken);

        // Create a user object with params, studentId is null
        ExtensionUser user = new ExtensionUser(bearerToken, userId, courseId, assignmentId, null, type);

        return evaluation.compileStudentCodeFile(user, files);
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