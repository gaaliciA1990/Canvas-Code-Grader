package com.canvas.service;

import com.canvas.controllers.chromeApiServer.ChromeApiController;
import com.canvas.exceptions.CanvasAPIException;
import com.canvas.service.helperServices.CanvasClientService;
import com.canvas.service.helperServices.FileService;
import com.canvas.service.helperServices.ProcessExecutor;
import com.canvas.service.models.CommandOutput;
import com.canvas.service.models.ExtensionUser;
import com.canvas.service.models.submission.Submission;
import com.canvas.service.models.submission.SubmissionFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * This class handles the Student side of the evaluation process.
 */
@Service
public class EvaluationService {
    private final SubmissionDirectoryService submissionDirectoryService;

    /**
     * Constructor for creating the SubmissionDirectoryService instance
     *
     * @param submissionDirectoryService SubmissionDirectoryService instance
     */
    @Autowired
    public EvaluationService(SubmissionDirectoryService submissionDirectoryService) {
        this.submissionDirectoryService = submissionDirectoryService;
    }

    /**
     * This method will compile the student code. The files are extracted
     * and the program is compiled.
     *
     * @param user  User object holding all components associated with a user
     * @param files The files to compile
     * @return return a CommandOutput response
     */
    public ResponseEntity<CommandOutput> compileStudentCodeFile(ExtensionUser user, MultipartFile[] files) throws CanvasAPIException, IOException {
        byte[] makefileBytes = getMakefileBytesIfExistsWithinStudentSubmissionFilesRetrievedFromEvaluatePostRequestBody(files);
        if (makefileBytes != null) {
            submissionDirectoryService.writeMakefileFromStudent(makefileBytes, user.getUserId());
        } else {
            // Retrieve file json from Canvas
            submissionDirectoryService.writeMakefileFromCanvas(user, user.getUserId());
        }

        // Write submitted code files
        submissionDirectoryService.writeSubmissionFiles(files, user.getUserId());

        // Compile the files and grab output
        CommandOutput commandOutput = compileCodeFiles(user.getUserId());

        // Cleanup
        submissionDirectoryService.deleteDirectory(user.getUserId());

        // Generate response
        return new ResponseEntity<>(commandOutput, HttpStatus.OK);
    }

    private byte[] getMakefileBytesIfExistsWithinStudentSubmissionFilesRetrievedFromEvaluatePostRequestBody(MultipartFile[] files) throws IOException {
        for (MultipartFile file : files) {
            if (file.getOriginalFilename().equalsIgnoreCase(SubmissionDirectoryService.MAKEFILE)) {
                return file.getBytes();
            }
        }
        return null;
    }


    /**
     * Helper method for compiling code files based on userId
     *
     * @param userId canvas User ID for naming executable
     * @return message associated with action
     */
    private CommandOutput compileCodeFiles(String userId) {
        CommandOutput compileOutput = executeCommand(new String[]{"make"}, userId);
        if (compileOutput.isSuccess()) {
            compileOutput.setOutput("Your program compiled successfully!");
        }
        return compileOutput;
    }

    /**
     * Helper method for executing process commands
     *
     * @param commands Array of commands to run for execution
     * @param userId   message associated with action
     * @return message associated with action
     */
    private CommandOutput executeCommand(String[] commands, String userId) {
        ProcessExecutor processExecutor = new ProcessExecutor(commands, submissionDirectoryService.getSubmissionDirectory(userId));
        boolean compileSuccess = processExecutor.executeProcess();
        String output = processExecutor.getProcessOutput();
        return new CommandOutput(compileSuccess, output);
    }

}
