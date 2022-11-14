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

@RestController
@CrossOrigin
public class ChromeApiController {

    public ChromeApiController() { }

    @PostMapping(
            value = "/evaluate",
            produces = { "application/json" },
            consumes = { "multipart/form-data" }
    )
    public ResponseEntity<CommandOutput> compileCodeFile(
            @RequestHeader("Authorization") String bearerToken,
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("userId") String userId,
            @RequestParam("assignmentId") String assignmentId,
            @RequestParam("courseId") String courseId
    ) {
        CanvasClientService canvasClientService = new CanvasClientService(bearerToken);
        String makefileName = "makefile";

        // Retrieve file json from Canvas
        byte[] makefileBytes = new byte[0];
        try {
            makefileBytes = canvasClientService.fetchFileUnderCourseAssignmentFolder(
                    courseId, assignmentId, makefileName
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Write makefile to file
        FileService fileService = FileService.getFileService(userId);
        fileService.writeFileFromBytes(makefileName, makefileBytes);

        // Write submitted code files
        for (MultipartFile file : files) {
            fileService.writeFileFromMultipart(file);
        }

        // Compile the files and grab output
        ProcessExecutor processExecutor = new ProcessExecutor(new String[] {"make"}, fileService.getFileDirectory());
        boolean compileSuccess = processExecutor.executeProcess();
        String output = compileSuccess ? "Your program compiled successfully!" : processExecutor.getProcessOutput();

        // Cleanup
        fileService.deleteDirectory();

        // Generate response
        CommandOutput commandOutput = new CommandOutput(compileSuccess, output);

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

}