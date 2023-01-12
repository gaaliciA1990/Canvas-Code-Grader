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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class handles the Student side of the evaluation process.
 */
@Service
public class EvaluationService {
    private final FileService fileService;
    private final CanvasClientService canvasClientService;

    private static final String MAKEFILE = "makefile";

    /**
     * Constructor for creating our instances of FileService and CanvasClientService
     *
     * @param fileService         File service instance
     * @param canvasClientService Canvas client service instance
     */
    @Autowired
    public EvaluationService(FileService fileService, CanvasClientService canvasClientService) {
        this.fileService = fileService;
        this.canvasClientService = canvasClientService;
    }

    /**
     * This method will compile the student code. The files are extracted
     * and the program is compiled.
     *
     * @param user  User object holding all components associated with a user
     * @param files The files to compile
     * @return return a CommandOutput response
     */
    public ResponseEntity<CommandOutput> compileStudentCodeFile(ExtensionUser user, MultipartFile[] files) throws CanvasAPIException {


        // Retrieve file json from Canvas
        byte[] makefileBytes = new byte[0];
        makefileBytes = canvasClientService.fetchFileUnderCourseAssignmentFolder(user, MAKEFILE);

        // Write makefile to file
        fileService.writeFileFromBytes(MAKEFILE, makefileBytes, user.getUserId());

        // Write submitted code files
        for (MultipartFile file : files) {
            fileService.writeFileFromMultipart(file, user.getUserId());
        }

        // Compile the files and grab output
        CommandOutput commandOutput = compileCodeFiles(user.getUserId());

        // Cleanup
        fileService.deleteDirectory(user.getUserId());

        // Generate response
        return new ResponseEntity<>(commandOutput, HttpStatus.OK);
    }

    /**
     * Method executes student submitted code for instructors/graders to grade.
     *
     * @param user User object for the extension
     * @return return a CommandOutput response
     */
    public ResponseEntity<CommandOutput> executeCodeFile(ExtensionUser user) throws CanvasAPIException {
        // Write makefile to directory
        writeMakefile(user, user.getUserId());

        // Fetch submission files from Canvas
        Map<String, byte[]> submissionMap = canvasClientService.fetchStudentSubmissionFileBytes(user);

        String userId = user.getUserId();

        // Write submission files to directory
        for (Map.Entry<String, byte[]> entry : submissionMap.entrySet()) {
            // Key: filename, Value: file bytes to write
            fileService.writeFileFromBytes(entry.getKey(), entry.getValue(), fileService.getFileDirectory(userId));
        }

        // If code compiles successfully
        if (compileCodeFiles(userId).isSuccess()) {
            // Execute the code
            CommandOutput codeExecutionOutput = executeCodeFiles(userId);
            System.out.println(codeExecutionOutput.getOutput());

            // Cleanup
            fileService.deleteDirectory(userId);

            return new ResponseEntity<>(codeExecutionOutput, HttpStatus.OK);
        } else {
            // Cleanup
            fileService.deleteDirectory(user.getStudentId());
            return ResponseEntity.badRequest().build();
        }
    }

    public ResponseEntity<Submission> generateSubmissionDirectory(ExtensionUser user) throws CanvasAPIException {
        Submission submission = canvasClientService.fetchStudentSubmission(user);
        String submissionDirectory = "12345"; // TODO hash(courseId, assignmentId, studentId)

        writeMakefile(user, submissionDirectory);
        SubmissionFile[] submissionFiles = writeSubmissionFiles(submission.getSubmissionFileBytes(), submissionDirectory);

        submission.setSubmissionFiles(submissionFiles);
        submission.setSubmissionDirectory(submissionDirectory);

        return new ResponseEntity<>(submission, HttpStatus.OK);
    }

    private SubmissionFile[] writeSubmissionFiles(Map<String, byte[]> submissionFilesBytes, String fileDirectory) {
        List<SubmissionFile> submissionFiles = new ArrayList<>();

        for (Map.Entry<String, byte[]> entry : submissionFilesBytes.entrySet()) {
            String fileName = entry.getKey();
            byte[] fileBytes = entry.getValue();
            fileService.writeFileFromBytes(fileName, fileBytes, fileDirectory);
            String[] fileContent = fileService.parseLinesFromFile(fileName, fileDirectory);
            submissionFiles.add(new SubmissionFile(fileName, fileContent));
        }

        return submissionFiles.toArray(new SubmissionFile[0]);
    }

    /**
     * Helper method for writing the makefile
     *
     * @param user User object the make file is associated with
     * @throws CanvasAPIException
     */
    private void writeMakefile(ExtensionUser user, String directory) throws CanvasAPIException {
        // Retrieve file json from Canvas
        byte[] makefileBytes = canvasClientService.fetchFileUnderCourseAssignmentFolder(user, MAKEFILE);

        fileService.writeFileFromBytes(MAKEFILE, makefileBytes, directory);
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
     * Helper method for executing the code files based on userId
     *
     * @param userId canvas User ID for naming executable
     * @return message associated with action
     */
    private CommandOutput executeCodeFiles(String userId) {
        String exeFile = fileService.getFileNameWithExtension(".exe", userId);
        return executeCommand(new String[]{exeFile}, userId);
    }

    /**
     * Helper method for executing process commands
     *
     * @param commands Array of commands to run for execution
     * @param userId   message associated with action
     * @return message associated with action
     */
    private CommandOutput executeCommand(String[] commands, String userId) {
        ProcessExecutor processExecutor = new ProcessExecutor(commands, fileService.getFileDirectory(userId));
        boolean compileSuccess = processExecutor.executeProcess();
        String output = processExecutor.getProcessOutput();
        return new CommandOutput(compileSuccess, output);
    }

}
