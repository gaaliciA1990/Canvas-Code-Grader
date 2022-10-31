package com.canvas.chromeApiServer;

import com.canvas.dto.CommandOutput;
import com.canvas.service.ProcessExecutor;
import com.canvas.service.FileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin
public class ChromeApiController {

    public ChromeApiController() {

    }

    @PostMapping(
            value = "/evaluate",
            produces = { "application/json" },
            consumes = { "multipart/form-data" }
    )
    public ResponseEntity<CommandOutput> compileCodeFile(@RequestParam("files") MultipartFile[] files) {
        FileService fileService = FileService.getFileService();
        List<String> commands = new ArrayList<>();
        commands.add("g++");

        // Write files
        for (MultipartFile file : files) {
            fileService.writeFile(file);
            commands.add(file.getOriginalFilename());
        }

        // Compile the files and grab output
        ProcessExecutor processExecutor = new ProcessExecutor(commands);
        boolean compileSuccess = processExecutor.executeProcess();
        String output = compileSuccess ? "Your program compiled successfully!" : processExecutor.getProcessOutput();

        // Cleanup
        for (MultipartFile file : files) {
            fileService.deleteFile(file);
        }
        fileService.deleteFile("a.exe"); // Executable
        fileService.deleteFilesEndingWithExtension(".gch"); // GCC Precompiled Header

        // Generate response
        CommandOutput commandOutput = new CommandOutput(compileSuccess, output);

        return new ResponseEntity<>(commandOutput, HttpStatus.OK);
    }
}