package com.canvas.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.web.multipart.MultipartFile;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

public class FileService {
    public FileService() {

    }

    public void writeFileFromDataBufferPublisher(Publisher<DataBuffer> dataBufferFlux) {
        File dir = new File("./");
        new File(dir, "makefile");
        Path path = Paths.get("makefile");
        DataBufferUtils.write(dataBufferFlux, path, CREATE_NEW).block();
    }

    public boolean writeFile(MultipartFile file) {
        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                String fileName = file.getOriginalFilename();
                FileOutputStream fos = new FileOutputStream(fileName);
                fos.write(bytes);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public boolean deleteFile(MultipartFile file) {
        return deleteFileHelper(file.getOriginalFilename());
    }

    public boolean deleteFile(String fileName) {
        return deleteFileHelper(fileName);
    }

    public boolean deleteFilesEndingWithExtension(String extension) {
        File dir = new File("./");
        File[] files = dir.listFiles((d, name) -> name.endsWith(extension));
        if (files != null && files.length > 0) {
            for (File file : files) {
                deleteFileHelper(file.getName());
            }
        }
        return true;
    }

    private boolean deleteFileHelper(String fileName) {
        boolean deleteSuccess = false;
        try {
            deleteSuccess = Files.deleteIfExists(Paths.get(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return deleteSuccess;
    }

    public static FileService getFileService() {
        return new FileService();
    }
}