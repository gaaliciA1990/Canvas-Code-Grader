package com.canvas.service.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExtensionUserTest {

    private ExtensionUser extensionUser;

    @BeforeEach
    public void before() {
        extensionUser = new ExtensionUser(
                "fooToken",
                "fooUserId",
                "fooCourseId",
                "fooAssignmentId",
                "fooStudentId",
                UserType.GRADER
        );
    }

    @Test
    public void testGetters() {
        Assertions.assertEquals("fooToken", extensionUser.getBearerToken());
        Assertions.assertEquals("fooUserId", extensionUser.getUserId());
        Assertions.assertEquals("fooCourseId", extensionUser.getCourseId());
        Assertions.assertEquals("fooAssignmentId", extensionUser.getAssignmentId());
        Assertions.assertEquals("fooStudentId", extensionUser.getStudentId());
        Assertions.assertEquals(UserType.GRADER, extensionUser.getUserType());
    }

    @Test
    public void testSetters() {
        // Given
        extensionUser.setBearerToken("fooToken2");
        extensionUser.setUserId("fooUserId2");
        extensionUser.setCourseId("fooCourseId2");
        extensionUser.setAssignmentId("fooAssignmentId2");
        extensionUser.setStudentId("fooStudentId2");
        extensionUser.setUserType(UserType.STUDENT);

        // Then
        Assertions.assertEquals("fooToken2", extensionUser.getBearerToken());
        Assertions.assertEquals("fooUserId2", extensionUser.getUserId());
        Assertions.assertEquals("fooCourseId2", extensionUser.getCourseId());
        Assertions.assertEquals("fooAssignmentId2", extensionUser.getAssignmentId());
        Assertions.assertEquals("fooStudentId2", extensionUser.getStudentId());
        Assertions.assertEquals(UserType.STUDENT, extensionUser.getUserType());
    }
}