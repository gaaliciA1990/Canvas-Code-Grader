package com.canvas.controllers.chromeApiServer;

import com.canvas.exceptions.CanvasAPIException;
import com.canvas.exceptions.IncorrectRequestParamsException;
import com.canvas.exceptions.UserNotAuthorizedException;
import com.canvas.service.EvaluationService;
import com.canvas.service.models.CommandOutput;
import com.canvas.service.helperServices.CanvasClientService;
import com.canvas.service.models.ExtensionUser;
import com.canvas.service.models.UserType;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public ChromeApiController(EvaluationService studentEval, CanvasClientService canvasClientService) {
        this.evaluation = studentEval;
        this.canvasClientService = canvasClientService;
    }


    /**
     * @param bearerToken
     * @param assignmentId
     * @param courseId
     * @param studentId
     * @return
     */
    @GetMapping(
            value = "/execute/courses/{courseId}/assignments/{assignmentId}/submissions/{studentId}",
            produces = {"application/json"}
    )
    public ResponseEntity<CommandOutput> initiateInstructorCodeEvaluation(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable("assignmentId") String assignmentId,
            @PathVariable("courseId") String courseId,
            @PathVariable("studentId") String studentId,
            @RequestParam("userType") UserType type
    ) throws UserNotAuthorizedException, IncorrectRequestParamsException, CanvasAPIException {
        // Check request params are correct, not null or empty.
        // TODO: What do we consider incorrect? Define further
        if (bearerToken == null || assignmentId == null || courseId == null || studentId == null || type == null) {
            String errMsg = "Incorrect request parameters received. Check the parameters meet the requirements";
            System.out.println(errMsg); // TODO: use logger for printing to console
            throw new IncorrectRequestParamsException(errMsg);
        }

        if (bearerToken.isEmpty() || assignmentId.isEmpty() || courseId.isEmpty() || studentId.isEmpty()) {
            String errMsg = "Incorrect request parameters received. Check the parameters meet the requirements";
            System.out.println(errMsg); // TODO: use logger for printing to console
            throw new IncorrectRequestParamsException(errMsg);
        }

        // check userType isn't Unauthorized or Student
        if (type == UserType.UNAUTHORIZED || type == UserType.STUDENT) {
            String errorMessage = String.format("user type [%s] does not match expected [%s]", type, UserType.GRADER);
            System.out.println(errorMessage); // TODO use spring boot logger for printing messages to console
            throw new UserNotAuthorizedException(errorMessage);
        }
        // get the user id from the Canvas API
        String userId = canvasClientService.fetchUserId(bearerToken);

        // Create the user with the params
        ExtensionUser user = new ExtensionUser(bearerToken, userId, courseId, assignmentId, studentId, type);

        return evaluation.executeCodeFile(user);

    }

    /**
     * Post request to start the evaluation service.
     * Depending on type of user, the correct evaluation path is called.
     * TODO: this should only return a success for starting the program
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
            String errMsg = "Incorrect request parameters received. Check the parameters meet the requirements";
            System.out.println(errMsg); // TODO: use logger for printing to console
            throw new IncorrectRequestParamsException(errMsg);
        }

        if (bearerToken.isEmpty() || files.length == 0 || assignmentId.isEmpty() || courseId.isEmpty()) {
            String errMsg = "Incorrect request parameters received. Check the parameters meet the requirements";
            System.out.println(errMsg); // TODO: use logger for printing to console
            throw new IncorrectRequestParamsException(errMsg);
        }
        // check userType isn't Unauthorized or Grader
        if (type == UserType.UNAUTHORIZED || type == UserType.GRADER) {
            String errorMessage = String.format("user type [%s] does not match expected [%s]", type, UserType.STUDENT);
            System.out.println(errorMessage); // TODO use spring boot logger for printing messages to console
            throw new UserNotAuthorizedException(errorMessage);
        }

        // Get the user id from Canvas API
        String userId = canvasClientService.fetchUserId(bearerToken);

        // Create a user object with params, studentId is null
        ExtensionUser user = new ExtensionUser(bearerToken, userId, courseId, assignmentId, null, type);

        return evaluation.compileStudentCodeFile(user, files);
    }

    /*    *//**
     * Test Route to get student submission given access token and studentId
     * Submission is expected to be saved under my files/CanvasCode/sample.cpp
     *
     * @param studentId
     * @param token
     * @return
     *//*
    @GetMapping(
            value = "/fetchStudentSubmission",
            produces = {"application/json"}
    )
    public ResponseEntity<String> fetchFileFromCanvasAndSave(@RequestParam("studentId") String studentId,
                                                             @RequestParam("accessToken") String token,
                                                             @RequestParam("fileName") String fileName) {
        CanvasClientService canvasClientService = new CanvasClientService();
        try {
            canvasClientService.fetchSubmissionFromMyFilesAndSave(fileName, token);
        } catch (Exception e) {
            return new ResponseEntity<>("ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("SAVED FILE", HttpStatus.OK);
    }*/

}