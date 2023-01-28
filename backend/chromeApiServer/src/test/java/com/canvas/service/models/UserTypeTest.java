package com.canvas.service.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UserTypeTest {

    @Test
    public void testStringToEnum() {
        Assertions.assertEquals(
                UserType.STUDENT,
                UserType.stringToEnum("STUDENT")
        );
    }
}
