package com.canvas.exceptions;

import java.io.Serial;

public class UserNotAuthorizedException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    public UserNotAuthorizedException() {
        super();
    }

    public UserNotAuthorizedException(String message) {
        super(message);
    }
}
