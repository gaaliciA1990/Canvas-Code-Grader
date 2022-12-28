package com.canvas.exceptions;

import java.io.Serial;

/**
 * Exception to be thrown with message for incorrect request parameters
 */
public class IncorrectRequestParamsException extends Exception {

    /**
     * static variable for serial
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructor that takes in a message to pass to the base exception class
     * overriding the constructor of Exception class
     *
     * @param message String message for the error
     */
    public IncorrectRequestParamsException(String message) {
        super(message);
    }

    /**
     * Constructor that takes no arg, but still overrides the constructor of Exception class
     */
    public IncorrectRequestParamsException() {
        super();
    }
}
