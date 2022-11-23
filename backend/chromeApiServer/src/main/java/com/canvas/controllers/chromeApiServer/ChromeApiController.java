package com.canvas.controllers.chromeApiServer;

import com.canvas.service.EvaluationService;
import com.canvas.service.models.CommandOutput;
import com.canvas.service.helperServices.CanvasClientService;
import com.canvas.service.models.ExtensionUser;
import com.canvas.service.models.UserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

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
            @RequestParam("userType") String type
    ) throws IOException {
        // check the user type isn't null
        validate_userType_String(type);

        // convert type to ENUM
        UserType userType = convert_userTypeTo_Enum(type);

        // check userType isn't Unauthorized or Student
        if (userType == UserType.UNAUTHORIZED || userType == UserType.STUDENT) {
            System.out.println(String.format("Exception: user type does not match expected [%s]", UserType.GRADER));
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        // get the user id from the Canvas API
        String userId = canvasClientService.fetchUserId(bearerToken);

        // Create the user with the params
        ExtensionUser user = new ExtensionUser(bearerToken, userId, courseId, assignmentId, studentId, userType);

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
            @RequestParam("userType") String type
    ) throws IOException {

        // check the user type isn't null
        validate_userType_String(type);

        // convert type to ENUM
        UserType userType = convert_userTypeTo_Enum(type);

        // check userType isn't Unauthorized or Grader
        if (userType == UserType.UNAUTHORIZED || userType == UserType.GRADER) {
            System.out.println(String.format("Exception: user type does not match expected [%s]", UserType.STUDENT));
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // Get the user id from Canvas API
        String userId = canvasClientService.fetchUserId(bearerToken);

        // Create a user object with params, studentId is null
        ExtensionUser user = new ExtensionUser(bearerToken, userId, courseId, assignmentId, null, userType);

        return evaluation.compileStudentCodeFile(user, files);
    }

    /**
     * TODO: Need a Get function to push the results of the evaluation service. This would be our CommandOutput that
     * is constantly being updated during the program run time.
     * Need to figure out how we
     */
    @GetMapping(
            value = "/evaluate/output",
            produces = {"application/json"}
    )
    public ResponseEntity<CommandOutput> getRunningProgramOutput(
            @RequestParam("Authorization") String bearerToken
    ) {
        return null;
    }


    /**
     * Test Route to get student submission given access token and studentId
     * Submission is expected to be saved under my files/CanvasCode/sample.cpp
     *
     * @param studentId
     * @param token
     * @return
     */
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
    }

    /**
     * Helper method for validating the userType string isn't null
     * @param type  String type passed as arg
     */
    private void validate_userType_String(String type){
        String errorMessage = "Exception: userType cannot be null";

        // check the user type isn't null
        if (type == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
        }
    }

    /**
     * Helper method for converting string type to UserType Enum
     * @param type  String type to convert
     * @return      UserType Enum
     */
    private UserType convert_userTypeTo_Enum(String type) {
        String errorMessage = "User type error: Only accepted values are [student] or [grader]";
        try {
            return UserType.stringToEnum(type);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
        }
    }
}