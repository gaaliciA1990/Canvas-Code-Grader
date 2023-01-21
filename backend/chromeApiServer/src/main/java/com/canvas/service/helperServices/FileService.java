package com.canvas.service.helperServices;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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
     * Constructor with no args
     */
    public FileService() {
    }

    /**
     * Generates a file directory based on passed id
     *
     * @param id canvas user id
     * @return string of names directory
     */
    protected String generateFileDirectory(String id) {
        return "./" + id;
    }

    /**
     * Generates a file directory and named based on canvas user id.
     *
     * @param file code files to be evaluated
     * @param id   Canvas user id
     */
    public void writeFileFromMultipart(MultipartFile file, String id) {
        String fileDirectory = generateFileDirectory(id);

        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                String fileName = file.getOriginalFilename();
                FileOutputStream fos = new FileOutputStream(new File(fileDirectory, fileName));
                fos.write(bytes);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Writes a file to the directory from a byte array
     *
     * @param fileName File name to be evaluated
     * @param bytes    byte array
     * @param id       Canvas user id
     */
    public void writeFileFromBytes(String fileName, byte[] bytes, String id) {
        String fileDirectory = generateFileDirectory(id);

        File dir = new File(fileDirectory);
        dir.mkdirs();
        try {
            FileOutputStream fos = new FileOutputStream(fileDirectory + "/" + fileName);
            fos.write(bytes);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves file name based on extension and userID
     *
     * @param ext extension of file
     * @param id  canvas user id
     * @return string of path for file
     */
    public String getFileNameWithExtension(String ext, String id) {
        File dir = new File(generateFileDirectory(id));
        File file = Objects.requireNonNull(dir.listFiles((d, name) -> name.endsWith(ext)))[0];
        return file.getPath();
    }

    /**
     * Parses each line from the file and stores each line in an array.
     *
     * @param fileName name of file
     * @param fileDirectory directory where file is located
     * @return array of each line from the file
     */
    public String[] parseLinesFromFile(String fileName, String fileDirectory) {
        List<String> fileLines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(concatFileDirAndName(fileDirectory, fileName)))) {
            String line;
            while ((line = br.readLine()) != null) {
                fileLines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileLines.toArray(new String[0]);
    }


    /**
     * Deletes the directory based on name
     *
     * @param id Canvas user id associated with the directory
     */
    public void deleteDirectory(String id) {
        String fileDirectory = generateFileDirectory(id);

        try {
            FileUtils.deleteDirectory(new File(fileDirectory));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Getter for the file directory, based on canvas user id
     *
     * @param id Canvas user id
     * @return Boolean response, true is successful, else false
     */
    public String getFileDirectory(String id) {
        return generateFileDirectory(id);
    }

    /**
     * Helper function for deleting a file
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
     * Helper function for concatonating file directory and name
     *
     * @param fileDirectory Our created directory for the files
     * @param fileName      Name of the file
     * @return String format of the directory and file name
     */
    protected static String concatFileDirAndName(String fileDirectory, String fileName) {
        return fileDirectory + "/" + fileName;
    }

    // UNUSED CODE FOUND BELOW. SOME PRIVATE METHODS CALLED FROM CODE BELOW

    /**
     * Deletes files with a specific extension
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
     * Write a file based on data buffer
     *
     * @param dataBufferFlux Publisher data
     * @param fileName       name of the file
     * @param id             canvas user id to find the file
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
     * Creates a file directory based on the url in Canvas
     *
     * @param url      Canvas url string
     * @param filename file name of code submission
     * @param id       Canvas user id, used for naming file
     * @return boolean result for success or fail
     */
    public boolean writeFileFromUrl(String url, String filename, String id) {
        String fileDirectory = generateFileDirectory(id);

        try {
            FileUtils.copyURLToFile(new URL(url), new File(fileDirectory, filename));
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
}