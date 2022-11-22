package com.canvas.service;

import com.canvas.service.models.CommandOutput;
import com.canvas.service.models.ExtensionUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class StudentEvaluationService {
    private final FileService fileService;
    private final CanvasClientService canvasClientService;

    /**
     * Constructor for creating our instances of FileService and CanvasClientService
     *
     * @param fileService         File service instance
     * @param canvasClientService Canvas client service instance
     */
    @Autowired
    public StudentEvaluationService(FileService fileService, CanvasClientService canvasClientService) {
        this.fileService = fileService;
        this.canvasClientService = canvasClientService;
    }

    /**
     * This method will compile the student code
     *
     * @param user  User object holding all components associated with a user
     * @param files The files to compile
     * @return return a CommandOutput response
     */
    public ResponseEntity<CommandOutput> compileStudentCodeFile(ExtensionUser user, MultipartFile[] files) {
        String makefileName = "makefile";

        // Retrieve file json from Canvas
        byte[] makefileBytes = new byte[0];
        try {
            makefileBytes = canvasClientService.fetchFileUnderCourseAssignmentFolder(user, makefileName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Write makefile to file
        fileService.writeFileFromBytes(makefileName, makefileBytes, user.getUserId());

        // Write submitted code files
        for (MultipartFile file : files) {
            fileService.writeFileFromMultipart(file, user.getUserId());
        }

        // Compile the files and grab output
        ProcessExecutor processExecutor = new ProcessExecutor(new String[]{"make"}, fileService.getFileDirectory(user.getUserId()));
        boolean compileSuccess = processExecutor.executeProcess();
        String output = compileSuccess ? "Your program compiled successfully!" : processExecutor.getProcessOutput();

        // Cleanup
        fileService.deleteDirectory(user.getUserId());

        // Generate response
        CommandOutput commandOutput = new CommandOutput(compileSuccess, output);

        return new ResponseEntity<>(commandOutput, HttpStatus.OK);
    }

}
