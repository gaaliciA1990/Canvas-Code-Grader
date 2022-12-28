package com.canvas.exceptions.handler;

import com.canvas.exceptions.IncorrectRequestParamsException;
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

    @ExceptionHandler(UserNotAuthorizedException.class)
    protected ResponseEntity<ErrorResponse> handleUserNotAuthorizedException(
            UserNotAuthorizedException exception
    ) {
        return buildErrorResponse(exception);
    }

    /**
     * Exception handler for incorrect request parameters. Calls the method for handling
     * this exception and returns a message built from ErrorResponse
     * @param e exception being handled
     * @return  JSON message of the error encountered
     */
    @ExceptionHandler(IncorrectRequestParamsException.class)
    public ResponseEntity<ErrorResponse> handleIncorrectRequestParamsExceptions(
            IncorrectRequestParamsException e) {
        return buildErrorResponse(e);
    }

    /**
     * Private method for building the exception message
     * @param exception any type of exception encountered
     * @return          a message in JSON form
     */
    private ResponseEntity<ErrorResponse> buildErrorResponse(Exception exception) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNAUTHORIZED, exception.getMessage());
        return new ResponseEntity<>(errorResponse, errorResponse.getStatus());
    }
}
