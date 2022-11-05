package com.canvas.chromeApiServer;

import com.canvas.dto.CommandOutput;
import com.canvas.service.ProcessExecutor;
import com.canvas.service.FileService;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

@RestController
@CrossOrigin
public class ChromeApiController {

    @Autowired
    private WebClient.Builder webClientBuilder;

    public ChromeApiController() {

    }

    @Bean
    public WebClient.Builder getWebClientBuilder(){
        return WebClient.builder();
    }

    @PostMapping(
            value = "/evaluate",
            produces = { "application/json" },
            consumes = { "multipart/form-data" }
    )
    public ResponseEntity<CommandOutput> compileCodeFile(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("userId") String userId) {
        Publisher<DataBuffer> makefileDataBufferFlux = getFileFromCanvas();
        FileService fileService = FileService.getFileService(userId);
        fileService.writeFileFromDataBufferPublisher(makefileDataBufferFlux, "makefile");

        // Write files
        for (MultipartFile file : files) {
            fileService.writeFile(file);
        }

        // Compile the files and grab output
        String cdToFileDirectory = "cd " + fileService.getFileDirectory();
        ProcessExecutor processExecutor = new ProcessExecutor(new String[] {cdToFileDirectory, "make"});
        boolean compileSuccess = processExecutor.executeProcess();
        String output = compileSuccess ? "Your program compiled successfully!" : processExecutor.getProcessOutput();

        // Cleanup
        for (MultipartFile file : files) {
            fileService.deleteFile(file);
        }
        fileService.deleteFile("makefile");
        fileService.deleteFilesEndingWithExtension(".exe"); // Executable
        fileService.deleteFilesEndingWithExtension(".gch"); // GCC Precompiled Header

        // Generate response
        CommandOutput commandOutput = new CommandOutput(compileSuccess, output);

        return new ResponseEntity<>(commandOutput, HttpStatus.OK);
    }

    private Publisher<DataBuffer> getFileFromCanvas() {
        // TODO this request needs to be unique to each assignment in order to grab the correct makefile
        // TODO update URL to Canvas API
        return webClientBuilder.build()
                .get()
                .uri("http://127.0.0.1:55321/makefile/hello-world")
                .accept(MediaType.APPLICATION_OCTET_STREAM) // TODO how does canvas send file over HTTP
                .retrieve()
                .bodyToFlux(DataBuffer.class);
    }
}