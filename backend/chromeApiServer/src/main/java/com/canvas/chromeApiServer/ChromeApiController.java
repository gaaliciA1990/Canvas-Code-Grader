package com.canvas.chromeApiServer;

import com.canvas.dto.CommandOutput;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;

@RestController
public class ChromeApiController {

    public ChromeApiController() {

    }

    @PostMapping(
            value = "/evaluate",
            produces = { "application/json" },
            consumes = { "multipart/form-data" }
    )
    public ResponseEntity<CommandOutput> compileCodeFile(@RequestParam("file") MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                FileOutputStream fos = new FileOutputStream(fileName);
                fos.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String[] commands = {"g++", fileName};
        ProcessExecutor processExecutor = new ProcessExecutor(commands);
        boolean compileSuccess = processExecutor.executeProcess();
        String output = compileSuccess ? "Your program compiled successfully!" : processExecutor.getProcessOutput();

        // TODO delete file once we're done with it

        CommandOutput commandOutput = new CommandOutput(compileSuccess, output);

        return new ResponseEntity<>(commandOutput, HttpStatus.OK);
    }
}