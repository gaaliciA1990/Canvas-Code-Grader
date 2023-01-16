package com.canvas.service.helperServices;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.DataBuffer;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileServiceTest {

    private FileService fileService;

    @Mock
    Publisher<DataBuffer> dataBufferPublisher;

    @Mock
    MultipartFile multipartFile;

    @BeforeEach
    public void before() {
        MockitoAnnotations.openMocks(this);
        fileService = new FileService();
    }

    @Test
    public void testGenerateFileDirectory() {
        Assertions.assertEquals(
                "./fooDirectory",
                fileService.generateFileDirectory("fooDirectory")
        );
    }

    @Test
    public void testGetFileDirectory() {
        Assertions.assertEquals(
                "./fooDirectory",
                fileService.getFileDirectory("fooDirectory")
        );
    }

    @Test
    public void testWriteFileFromMultipart() throws IOException {
        byte[] bytes = {1, 2, 3};

        // TODO: original method is dependent on a file directory already being created. Is this correct?
        File dir = new File("./fooId");
        dir.mkdirs();

        when(multipartFile.getBytes()).thenReturn(bytes);
        when(multipartFile.getOriginalFilename()).thenReturn("fooOriginalFileName.txt");

        fileService.writeFileFromMultipart(multipartFile, "fooId");

        // Verify contents
        byte[] data = Files.readAllBytes(Path.of("./fooId/fooOriginalFileName.txt"));

        for (int i=0; i<3; i++) {
            assertEquals(bytes[i], data[i]);
        }

        // Delete directory
        Files.deleteIfExists(Path.of("./fooId/fooOriginalFileName.txt"));
        File directory = new File("fooId");
        directory.delete();
    }

    @Test
    public void testWriteFileFromMultipart_throwsIOException() throws IOException {
        File dir = new File("./fooId");
        dir.mkdirs();

        when(multipartFile.getBytes()).thenThrow(IOException.class);
        fileService.writeFileFromMultipart(multipartFile, "fooId");

        // For line coverage
        File directory = new File("fooId");
        directory.delete();
    }

    @Test
    public void testWriteFileFromBytes() throws IOException {
        byte[] bytes = {1, 2, 3};
        fileService.writeFileFromBytes("fileName", bytes, "fooId");

        // Get Contents
        byte[] data = Files.readAllBytes(Path.of("./fooId/fileName"));

        // Verify
        for (int i=0; i<3; i++) {
            assertEquals(bytes[i], data[i]);
        }

        // Delete directory
        Files.deleteIfExists(Path.of("./fooId/fileName"));
        File directory = new File("fooId");
        directory.delete();
    }

    @Test
    public void testGetFileNameWithExtension() throws IOException {
        File dir = new File("./fooId");
        dir.mkdirs();

        File txtFile = new File("./fooId/foo.txt");
        byte[] bytes = {1, 2, 3};
        fileService.writeFileFromBytes("fileName.txt", bytes, "fooId");
        String fileName = fileService.getFileNameWithExtension(".txt", "fooId");

        Assertions.assertEquals("./fooId/fileName.txt", fileName);

        Files.deleteIfExists(Path.of("./fooId/fileName.txt"));
        File directory = new File("fooId");
        directory.delete();
    }

    @Test
    public void testDeleteDirectory() {
        File dir = new File("./fooId");
        dir.mkdirs();

        fileService.deleteDirectory("fooId");

        Assertions.assertFalse(dir.exists());
    }

    @Test
    public void testConcatFileNameAndDir() {
        assertEquals("fooId/fooFile", FileService.concatFileDirAndName("fooId", "fooFile"));
    }

}