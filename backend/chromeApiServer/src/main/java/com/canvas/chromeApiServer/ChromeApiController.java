package com.canvas.chromeApiServer;

import com.canvas.dto.CommandOutput;
import com.canvas.service.CanvasClientService;
import com.canvas.service.ProcessExecutor;
import com.canvas.service.FileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
public class ChromeApiController {

    private static final String MAKEFILE = "makefile";
    public ChromeApiController() { }

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
        CanvasClientService canvasClientService = new CanvasClientService(bearerToken);
        FileService fileService = new FileService(studentId);

        // Write makefile to directory
        writeMakefile(courseId, assignmentId, canvasClientService, fileService);

        // Fetch submission files from Canvas
        Map<String, byte[]> submissionMap = new HashMap<>();
        try {
            submissionMap = canvasClientService.fetchSubmissionFilesFromStudent(courseId, assignmentId, studentId);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Write submission files to directory
        for (Map.Entry<String, byte[]> entry : submissionMap.entrySet()) {
            // Key: filename, Value: file bytes to write
            fileService.writeFileFromBytes(entry.getKey(), entry.getValue());
        }

        // If code compiles successfully
        if (compileCodeFiles(fileService).isSuccess()) {
            // Execute the code
            CommandOutput codeExecutionOutput = executeCodeFiles(fileService);
            System.out.println(codeExecutionOutput.getOutput());

            // Cleanup
            fileService.deleteDirectory();

            return new ResponseEntity<>(codeExecutionOutput, HttpStatus.OK);
        } else {
            // Cleanup
            fileService.deleteDirectory();
            return ResponseEntity.badRequest().build();
        }

    }

    @PostMapping(
            value = "/evaluate",
            produces = { "application/json" },
            consumes = { "multipart/form-data" }
    )
    public ResponseEntity<CommandOutput> compileCodeFile(
            @RequestHeader("Authorization") String bearerToken,
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("assignmentId") String assignmentId,
            @RequestParam("courseId") String courseId
    ) {
        CanvasClientService canvasClientService = new CanvasClientService(bearerToken);

        // Fetch student's userId
        String userId = "";
        try {
            userId = canvasClientService.fetchUserId();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileService fileService = FileService.getFileService(userId);

        // Write makefile to directory
        writeMakefile(courseId, assignmentId, canvasClientService, fileService);

        // Write submitted code files to directory
        for (MultipartFile file : files) {
            fileService.writeFileFromMultipart(file);
        }

        // Compile the files and grab output
        CommandOutput commandOutput = compileCodeFiles(fileService);

        // Cleanup
        fileService.deleteDirectory();

        return new ResponseEntity<>(commandOutput, HttpStatus.OK);
    }

    /**
     * Test Route to get student submission given access token and studentId
     * Submission is expected to be saved under my files/CanvasCode/sample.cpp
     * @param studentId
     * @param token
     * @return
     */

        @GetMapping(
            value = "/fetchStudentSubmission",
            produces = { "application/json" }
    )

    public ResponseEntity<String> fetchFileFromCanvasAndSave(@RequestParam("studentId") String studentId,
                                                  @RequestParam("accessToken") String token,
                                                  @RequestParam("fileName") String fileName  ) {
        CanvasClientService canvasClientService = new CanvasClientService("Bearer " + token);
        try {
        canvasClientService.fetchSubmissionFromMyFilesAndSave(fileName);
        }
        catch(Exception e) {
            return new ResponseEntity<>("ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("SAVED FILE", HttpStatus.OK);
    }

    private void writeMakefile(
            String courseId,
            String assignmentId,
            CanvasClientService canvasClientService,
            FileService fileService
    ) {
        // Retrieve file json from Canvas
        byte[] makefileBytes = new byte[0];
        try {
            makefileBytes = canvasClientService.fetchFileUnderCourseAssignmentFolder(
                    courseId, assignmentId, MAKEFILE
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        fileService.writeFileFromBytes(MAKEFILE, makefileBytes);
    }

    private static CommandOutput compileCodeFiles(FileService fileService) {
        CommandOutput compileOutput = executeCommand(new String[] {"make"}, fileService);
        if (compileOutput.isSuccess()) {
            compileOutput.setOutput("Your program compiled successfully!");
        }
        return compileOutput;
    }

    private static CommandOutput executeCodeFiles(FileService fileService) {
        String exeFile = fileService.getFileNameWithExtension(".exe");
        return executeCommand(new String[] {exeFile}, fileService);
    }

    private static CommandOutput executeCommand(String[] commands, FileService fileService) {
        ProcessExecutor processExecutor = new ProcessExecutor(commands, fileService.getFileDirectory());
        boolean compileSuccess = processExecutor.executeProcess();
        String output = processExecutor.getProcessOutput();
        return new CommandOutput(compileSuccess, output);
    }

}