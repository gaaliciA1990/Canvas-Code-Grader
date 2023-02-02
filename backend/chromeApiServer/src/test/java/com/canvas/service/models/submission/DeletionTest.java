package com.canvas.service.models.submission;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DeletionTest {

    private Deletion deletion;

    @BeforeEach
    void setUp() {
        deletion = new Deletion(
                true,
                "testDescription"
        );
    }

    @Test
    public void testGetSuccess() {
        Assertions.assertTrue(deletion.isSuccess());
    }

    @Test
    public void testGetDescription() {
        Assertions.assertEquals("testDescription", deletion.getDescription());
    }

    @Test
    public void testBuilder() {
        Deletion deletionBuild = Deletion.builder()
                .success(true)
                .description("testDescription")
                .build();

        Assertions.assertTrue(deletionBuild.isSuccess());
        Assertions.assertEquals("testDescription", deletionBuild.getDescription());

    }
}
