package com.canvas.service;

import com.canvas.service.helperServices.CanvasClientService;
import com.canvas.service.helperServices.FileService;
import com.canvas.service.helperServices.ProcessExecutor;
import com.canvas.service.models.CommandOutput;
import com.canvas.service.models.ExtensionUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class handles the Student side of the evaluation process.
 */
@Service
public class EvaluationService {
    private final FileService fileService;
    private final CanvasClientService canvasClientService;
    String MAKEFILE = "makefile";

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
     * @return      return a CommandOutput response
     */
    public ResponseEntity<CommandOutput> compileStudentCodeFile(ExtensionUser user, MultipartFile[] files) {


        // Retrieve file json from Canvas
        byte[] makefileBytes = new byte[0];
        try {
            makefileBytes = canvasClientService.fetchFileUnderCourseAssignmentFolder(user, MAKEFILE);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
     * @param user  User object for the extension
     * @return      return a CommandOutput response
     */
   public ResponseEntity<CommandOutput> executeCodeFile(ExtensionUser user) {
       // Write makefile to directory
       writeMakefile(user);

       // Fetch submission files from Canvas
       Map<String, byte[]> submissionMap = new HashMap<>();
       try {
           submissionMap = canvasClientService.fetchSubmissionFilesFromStudent(user);
       } catch (IOException e) {
           e.printStackTrace();
       }

       // Write submission files to directory
       for (Map.Entry<String, byte[]> entry : submissionMap.entrySet()) {
           // Key: filename, Value: file bytes to write
           fileService.writeFileFromBytes(entry.getKey(), entry.getValue(), fileService.getFileDirectory(user.getStudentId()));
       }

       // If code compiles successfully
       if (compileCodeFiles(user.getStudentId()).isSuccess()) {
           // Execute the code
           CommandOutput codeExecutionOutput = executeCodeFiles(user.getStudentId());
           System.out.println(codeExecutionOutput.getOutput());

           // Cleanup
           fileService.deleteDirectory(user.getStudentId());

           return new ResponseEntity<>(codeExecutionOutput, HttpStatus.OK);
       } else {
           // Cleanup
           fileService.deleteDirectory(user.getStudentId());
           return ResponseEntity.badRequest().build();
       }
   }

    private void writeMakefile(ExtensionUser user) {
        // Retrieve file json from Canvas
        byte[] makefileBytes = new byte[0];
        try {
            makefileBytes = canvasClientService.fetchFileUnderCourseAssignmentFolder(user, MAKEFILE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        fileService.writeFileFromBytes(MAKEFILE, makefileBytes, user.getUserId());
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
