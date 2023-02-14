package com.canvas.exceptions.handler;

import com.canvas.exceptions.CanvasAPIException;
import com.canvas.exceptions.IncorrectRequestParamsException;
import com.canvas.exceptions.MakefileNotFoundException;
import com.canvas.exceptions.UserNotAuthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Exceptions that are thrown in the controller will get redirected to methods in this class. To redirect
 * exception handling to a specific method, the @ExceptionHandler annotation is added with the exception
 * that it handles.
 */
@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Exception handler for unauthorized users. Calls method for handling this exception
     * and return a message built from ErrorResponse
     *
     * @param e exception being handled
     * @return message object from errorResponse class for the error encountered
     */
    @ExceptionHandler(UserNotAuthorizedException.class)
    protected ResponseEntity<ErrorResponse> handleUserNotAuthorizedException(
            UserNotAuthorizedException e
    ) {
        return buildErrorResponse(e, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Exception handler for incorrect request parameters. Calls the method for handling
     * this exception and returns a message built from ErrorResponse
     * @param e exception being handled
     * @return  message object from errorResponse class for the error encountered
     */
    @ExceptionHandler(IncorrectRequestParamsException.class)
    public ResponseEntity<ErrorResponse> handleIncorrectRequestParamsExceptions(
            IncorrectRequestParamsException e ) {
        return buildErrorResponse(e, HttpStatus.BAD_REQUEST);
    }

    /**
     * Exception handler for incorrect request parameters. Calls the method for handling
     * this exception and returns a message build from ErrorResponse
     * @param e exception being handled
     * @return  message object from errorResponse class for the error encountered
     */
    @ExceptionHandler(CanvasAPIException.class)
    public ResponseEntity<ErrorResponse> handleCanvasAPIException(
            CanvasAPIException e) {
        return buildErrorResponse(e, HttpStatus.FAILED_DEPENDENCY);
    }

    /**
     * Private method for building the exception message
     * @param exception any type of exception encountered
     * @param status    HTTP status code
     * @return          a message object from errorResponse class
     */
    private ResponseEntity<ErrorResponse> buildErrorResponse(Exception exception, HttpStatus status) {
        ErrorResponse errorResponse = new ErrorResponse(status, exception.getMessage());
        return new ResponseEntity<>(errorResponse, errorResponse.getStatus());
    }
}
