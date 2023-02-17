package com.canvas.exceptions;

import java.io.Serial;

public class MakefileNotFoundException extends CanvasAPIException {
    @Serial
    private static final long serialVersionUID = 1L;

    public MakefileNotFoundException(String message, Exception e) {
        super(message, e);
    }

    public MakefileNotFoundException(String message) {
        super(message);
    }

    public MakefileNotFoundException() {
        super("Makefile was not found in the assignment folder. Please submit a makefile " +
                "if required to do so. Otherwise, please contact your instructor.");
    }
}
