package com.canvas.service.models.submission;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.*;

class SubmissionTest {

    private Submission submission;
    private static final byte[] TEST_FILE_BYTE = "test".getBytes();

    @BeforeEach
    void setUp() {
        submission = new Submission(
                "testSubmissionId",
                "testStudentId",
                "testAssignmentId",
                new SubmissionFile[] {new SubmissionFile("test.cpp", new String[] {"test"})},
                "testDirectory",
                Map.ofEntries(entry("testName", TEST_FILE_BYTE))
        );
    }

    @Test
    void testGetSubmissionId() {
        assertEquals("testSubmissionId", submission.getSubmissionId());
    }

    @Test
    void testGetStudentId() {
        assertEquals("testStudentId", submission.getStudentId());
    }

    @Test
    void testGetAssignmentId() {
        assertEquals("testAssignmentId", submission.getAssignmentId());
    }

    @Test
    void testGetSubmissionFiles() {
        // Act
        SubmissionFile[] result = submission.getSubmissionFiles();

        // Assert
        assertEquals("test.cpp", result[0].getName());
        assertEquals("test", result[0].getFileContent()[0]);
    }

    @Test
    void testGetSubmissionDirectory() {
        assertEquals("testDirectory", submission.getSubmissionDirectory());
    }

    @Test
    void getSubmissionFileBytes() {
        // Act
        Map<String, byte[]> result = submission.getSubmissionFileBytes();

        // Assert
        assertEquals(TEST_FILE_BYTE, result.get("testName"));
    }

    @Test
    void testSetSubmissionFiles() {
        // Arrange
        SubmissionFile submissionFileFoo = new SubmissionFile("foo.cpp", new String[] {"foo"});
        SubmissionFile submissionFileBar = new SubmissionFile("bar.cpp", new String[] {"bar"});
        SubmissionFile[] submissionFiles = { submissionFileFoo, submissionFileBar };

        // Act
        submission.setSubmissionFiles(submissionFiles);
        SubmissionFile[] result = submission.getSubmissionFiles();

        // Assert
        assertEquals("foo.cpp", result[0].getName());
        assertEquals("foo", result[0].getFileContent()[0]);
        assertEquals("bar.cpp", result[1].getName());
        assertEquals("bar", result[1].getFileContent()[0]);
    }

    @Test
    void setSubmissionDirectory() {
        // Act
        submission.setSubmissionDirectory("fooDirectory");

        // Assert
        assertEquals("fooDirectory", submission.getSubmissionDirectory());
    }

    @Test
    void testBuilder() {
        Submission submissionByBuilder = Submission.builder()
                .submissionId("fooSubmissionId")
                .assignmentId("fooAssignmentId")
                .studentId("fooStudentId")
                .submissionFileBytes(Map.ofEntries(entry("testName", TEST_FILE_BYTE)))
                .build();

        assertEquals("fooSubmissionId", submissionByBuilder.getSubmissionId());
        assertEquals("fooAssignmentId", submissionByBuilder.getAssignmentId());
        assertEquals("fooStudentId", submissionByBuilder.getStudentId());
        assertEquals(TEST_FILE_BYTE, submissionByBuilder.getSubmissionFileBytes().get("testName"));
        assertNull(submissionByBuilder.getSubmissionDirectory());
        assertNull(submissionByBuilder.getSubmissionFiles());
    }
}