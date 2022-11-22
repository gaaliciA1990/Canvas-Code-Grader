package com.canvas.controllers.chromeApiServer;

import com.canvas.service.StudentEvaluationService;
import com.canvas.service.models.CommandOutput;
import com.canvas.service.CanvasClientService;
import com.canvas.service.models.ExtensionUser;
import com.canvas.service.models.UserType;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin
public class ChromeApiController {
    private final StudentEvaluationService studentEval;

    @Autowired
    public ChromeApiController(StudentEvaluationService studentEval) {
        this.studentEval = studentEval;
    }


    /**
     * Post request to evaluate files. Depending on type of user, the correct evaluation path is called.
     *
     * @param bearerToken  authorization token from Canvas API
     * @param files        file(s) to be evaluated
     * @param userId       Canvas user id
     * @param assignmentId Canvas assignment id
     * @param courseId     Canvas course id
     * @param type         User Type Enum
     * @return Response CommandOutput
     */

    @GetMapping(
            value = "/execute/courses/{courseId}/assignments/{assignmentId}/submissions/{studentId}",
            produces = { "application/json" }
    )
    public ResponseEntity<CommandOutput> executeCodeFiles(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable("assignmentId") String assignmentId,
            @PathVariable("courseId") String courseId,
            @PathVariable("studentId") String studentId
    ) {
        // Write makefile to directory
        writeMakefile(studentId, courseId, assignmentId, bearerToken);

        // Fetch submission files from Canvas
        Map<String, byte[]> submissionMap = new HashMap<>();
        try {
            submissionMap = canvasClientService.fetchSubmissionFilesFromStudent(courseId, assignmentId, studentId, bearerToken);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Write submission files to directory
        for (Map.Entry<String, byte[]> entry : submissionMap.entrySet()) {
            // Key: filename, Value: file bytes to write
            fileService.writeFileFromBytes(entry.getKey(), entry.getValue(), fileService.getFileDirectory(studentId));
        }

        // If code compiles successfully
        if (compileCodeFiles(studentId).isSuccess()) {
            // Execute the code
            CommandOutput codeExecutionOutput = executeCodeFiles(studentId);
            System.out.println(codeExecutionOutput.getOutput());

            // Cleanup
            fileService.deleteDirectory(studentId);

            return new ResponseEntity<>(codeExecutionOutput, HttpStatus.OK);
        } else {
            // Cleanup
            fileService.deleteDirectory(studentId);
            return ResponseEntity.badRequest().build();
        }

    }

    @PostMapping(
            value = "/evaluate",
            produces = {"application/json"},
            consumes = {"multipart/form-data"}
    )
    public ResponseEntity<CommandOutput> initiateCodeEvaluation(
            @RequestHeader("Authorization") String bearerToken,
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("assignmentId") String assignmentId,
            @RequestParam("courseId") String courseId,
            @RequestParam("userType") UserType type
    ) {
        ExtensionUser user = new ExtensionUser(bearerToken, userId, courseId, assignmentId, type);

        if (type == UserType.STUDENT) {
            return studentEval.compileStudentCodeFile(user, files);
        }
        if (type == UserType.GRADER) {
            // TODO: Implement the path for the Instructor side
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            System.out.println(String.format("Exception: user type does not match expected [%s] or [%s]", UserType.GRADER, UserType.STUDENT));
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping(
            value = "/fetchStudentSubmission",
            produces = {"application/json"}
    )

    /**
     * Test Route to get student submission given access token and studentId
     * Submission is expected to be saved under my files/CanvasCode/sample.cpp
     * @param studentId
     * @param token
     * @return
     */
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

    private void writeMakefile(
            String userId,
            String courseId,
            String assignmentId,
            String bearerToken
    ) {
        // Retrieve file json from Canvas
        byte[] makefileBytes = new byte[0];
        try {
            makefileBytes = canvasClientService.fetchFileUnderCourseAssignmentFolder(courseId, assignmentId, MAKEFILE, bearerToken);
        } catch (IOException e) {
            e.printStackTrace();
        }

        fileService.writeFileFromBytes(MAKEFILE, makefileBytes, userId);
    }

    private CommandOutput compileCodeFiles(String userId) {
        CommandOutput compileOutput = executeCommand(new String[] {"make"}, userId);
        if (compileOutput.isSuccess()) {
            compileOutput.setOutput("Your program compiled successfully!");
        }
        return compileOutput;
    }

    private CommandOutput executeCodeFiles(String userId) {
        String exeFile = fileService.getFileNameWithExtension(".exe", userId);
        return executeCommand(new String[] {exeFile}, userId);
    }

    private CommandOutput executeCommand(String[] commands, String userId) {
        ProcessExecutor processExecutor = new ProcessExecutor(commands, fileService.getFileDirectory(userId));
        boolean compileSuccess = processExecutor.executeProcess();
        String output = processExecutor.getProcessOutput();
        return new CommandOutput(compileSuccess, output);
    }

}