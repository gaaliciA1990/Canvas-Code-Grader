package com.canvas.service.helperServices;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import static java.nio.file.StandardOpenOption.WRITE;

/**
 * This class handles all file management
 */
@Service
public class FileService {

    /**
     * Constructor
     */
    public FileService() {
    }

    private String generateFileDirectory(String id) {
        return "./" + id;
    }

    /**
     * TODO: What is this method creating?
     *
     * @param dataBufferFlux
     * @param fileName
     * @param id
     */
    public void writeFileFromDataBufferPublisher(Publisher<DataBuffer> dataBufferFlux, String fileName, String id) {
        String fileDirectory = generateFileDirectory(id);

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

    /**
     * Creates a file directory based on the url in Canvas TODO: please clarify
     *
     * @param url      Canvas url string
     * @param filename file name of code submission
     * @param id       Canvas user id, used for naming file
     * @return
     */
    public boolean writeFileFromUrl(String url, String filename, String id) {
        String fileDirectory = generateFileDirectory(id);

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

    /**
     * Generates a file directory and named based on canvas user id.
     *
     * @param file code files to be evaluated
     * @param id   Canvas user id
     * @return Boolean response, true is successful, else false
     */
    public boolean writeFileFromMultipart(MultipartFile file, String id) {
        String fileDirectory = generateFileDirectory(id);

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


    /**
     * Creates a file directory from a byte array
     *
     * @param fileName File name to be evaluated
     * @param bytes    byte array
     * @param id       Canvas user id
     * @return Boolean response, true is successful, else false
     */
    public boolean writeFileFromBytes(String fileName, byte[] bytes, String id) {
        String fileDirectory = generateFileDirectory(id);

        File dir = new File(fileDirectory);
        dir.mkdirs();
        try {
            FileOutputStream fos = new FileOutputStream(fileDirectory + "/" + fileName);
            fos.write(bytes);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public String getFileNameWithExtension(String ext, String id) {
        File dir = new File(generateFileDirectory(id));
        File file = Objects.requireNonNull(dir.listFiles((d, name) -> name.endsWith(ext)))[0];
        return file.getPath();
    }

    /**
     * Deletes the directory based on name
     *
     * @param id Canvas user id associated with the directory
     * @return Boolean response, true is successful, else false
     */
    public boolean deleteDirectory(String id) {
        String fileDirectory = generateFileDirectory(id);

        try {
            FileUtils.deleteDirectory(new File(fileDirectory));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    /**
     * Deletes all files from the directory based on multipart files
     *
     * @param file Mulitpart files to be deleted
     * @param id   canvas user id
     * @return Boolean response, true is successful, else false
     */
    public boolean deleteFile(MultipartFile file, String id) {
        String fileDirectory = generateFileDirectory(id);

        return deleteFileHelper(concatFileDirAndName(fileDirectory, file.getOriginalFilename()), id);
    }

    /**
     * Deletes a file from the directory
     *
     * @param fileName name of the file to be deleted
     * @param id       canvas user id
     * @return Boolean response, true is successful, else false
     */
    public boolean deleteFile(String fileName, String id) {
        return deleteFileHelper(fileName, id);
    }

    /**
     * TODO: What is this deleting?
     *
     * @param extension File extension
     * @param id        canvas user id
     * @return Boolean response, true is successful, else false
     */
    public boolean deleteFilesEndingWithExtension(String extension, String id) {
        String fileDirectory = generateFileDirectory(id);

        File dir = new File(fileDirectory);
        File[] files = dir.listFiles((d, name) -> name.endsWith(extension));
        if (files != null && files.length > 0) {
            for (File file : files) {
                deleteFileHelper(file.getName(), id);
            }
        }
        return true;
    }

    /**
     * Deletes a helper file
     *
     * @param fileName Name of the file to be deleted
     * @param id       Canvas user id
     * @return Boolean response, true is successful, else false
     */
    private boolean deleteFileHelper(String fileName, String id) {
        String fileDirectory = generateFileDirectory(id);

        boolean deleteSuccess = false;
        try {
            deleteSuccess = Files.deleteIfExists(Paths.get(concatFileDirAndName(fileDirectory, fileName)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return deleteSuccess;
    }

    /**
     * Getter for the file directory, based on canvas user id
     *
     * @param id Canvas user id
     * @return Boolean response, true is successful, else false
     */
    public String getFileDirectory(String id) {
        String fileDirectory = generateFileDirectory(id);

        return fileDirectory;
    }

    /**
     * Helper function for concatonating file directory and name
     * TODO: What is this doing?
     *
     * @param fileDirectory  Our created directory for the files
     * @param fileName       Name of the file
     * @return               String format of the directory and file name
     */
    private static String concatFileDirAndName(String fileDirectory, String fileName) {
        return fileDirectory + "/" + fileName;
    }
}
