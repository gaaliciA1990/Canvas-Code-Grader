package com.canvas.service.models.submission;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class SubmissionFileTest {

    private SubmissionFile submissionFile;

    @BeforeEach
    void setUp() {
        submissionFile = new SubmissionFile("hello.cpp", new String[] {"Hello", "World"});
    }

    @Test
    void getName() {
        assertEquals("hello.cpp", submissionFile.getName());
    }

    @Test
    void getFileContent() {
        // Act
        String[] fileContent = submissionFile.getFileContent();

        // Assert
        assertArrayEquals(new String[] {"Hello", "World"}, fileContent);
        assertEquals("Hello", fileContent[0]);
        assertEquals("World", fileContent[1]);
    }

    @Test
    void setName() {
        // Act
        submissionFile.setName("foo.cpp");

        // Assert
        assertEquals("foo.cpp", submissionFile.getName());
    }

    @Test
    void setFileContent() {
        // Act
        submissionFile.setFileContent(new String[] {"foo", "bar"});
        String[] fileContent = submissionFile.getFileContent();

        // Assert
        assertFalse(Arrays.equals(new String[] {"Hello", "World"}, fileContent));
        assertEquals("foo", fileContent[0]);
        assertEquals("bar", fileContent[1]);
    }
}