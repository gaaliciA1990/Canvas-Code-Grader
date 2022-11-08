package com.canvas.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.web.multipart.MultipartFile;

import static java.nio.file.StandardOpenOption.WRITE;

public class FileService {

    private final String fileDirectory;

    public FileService(String id) {
        this.fileDirectory = generateFileDirectory(id);
    }

    private String generateFileDirectory(String id) {
        return "./" + id;
    }

    public void writeFileFromDataBufferPublisher(Publisher<DataBuffer> dataBufferFlux, String fileName) {
        // create directory
        File dir = new File(fileDirectory);
        dir.mkdirs();

        // create makefile inside of directory
        File newFile = new File(dir, fileName);
        try {
            newFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // write to makefile
        Path path = Paths.get(newFile.getPath());
        DataBufferUtils.write(dataBufferFlux, path, WRITE).block();
    }

    public boolean writeFileFromUrl(String url, String filename) {
        try {
            FileUtils.copyURLToFile(
                    new URL(url),
                    new File(fileDirectory, filename)
            );
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean writeFileFromMultipart(MultipartFile file) {
        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                String fileName = file.getOriginalFilename();
                FileOutputStream fos = new FileOutputStream(
                        new File(fileDirectory, fileName)
                );
                fos.write(bytes);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public boolean deleteDirectory() {
        try {
            FileUtils.deleteDirectory(new File(fileDirectory));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean deleteFile(MultipartFile file) {
        return deleteFileHelper(concatFileDirAndName(fileDirectory, file.getOriginalFilename()));
    }

    public boolean deleteFile(String fileName) {
        return deleteFileHelper(fileName);
    }

    public boolean deleteFilesEndingWithExtension(String extension) {
        File dir = new File(fileDirectory);
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
            deleteSuccess = Files.deleteIfExists(Paths.get(concatFileDirAndName(fileDirectory, fileName)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return deleteSuccess;
    }

    public static FileService getFileService(String id) {
        return new FileService(id);
    }

    public String getFileDirectory() {
        return fileDirectory;
    }

    private static String concatFileDirAndName(String fileDirectory, String fileName) {
        return fileDirectory + "/" + fileName;
    }
}
