package com.canvas.chromeApiServer;

import com.canvas.config.CanvasConfiguration;
import com.canvas.dto.CommandOutput;
import com.canvas.service.CanvasClientService;
import com.canvas.service.ProcessExecutor;
import com.canvas.service.FileService;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

@RestController
@CrossOrigin
public class ChromeApiController {
    @Autowired
    private CanvasConfiguration canvasConfig;

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
            @RequestParam("userId") String userId
    ) {
        // Retrieve file json from Canvas
        byte[] makefileBytes = new byte[0];
        try {
            makefileBytes = new CanvasClientService("Bearer " + this.canvasConfig.getAuthToken())
                    .fetchFile("68687639");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Write makefile to file
        String makefileName = "makefile";
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
//    private Publisher<DataBuffer> getFileFromCanvas(String fileId) {
//        return webClientBuilder.build()
//                .get()
//                .uri(CANVAS_HOST_URL + "/files/" + fileId)
//                .header("Authorization", "Bearer " + CANVAS_AUTH_TOKEN)
//                .accept(MediaType.APPLICATION_JSON)
//                .retrieve()
//                .bodyToFlux(DataBuffer.class);
//    }
}