package com.canvas.exceptions;

import java.io.Serial;

/**
 * Todo: @Francis, add comments to methods and class. Write tests for your code please
 */
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
