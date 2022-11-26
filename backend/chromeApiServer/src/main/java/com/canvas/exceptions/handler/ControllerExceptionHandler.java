package com.canvas.exceptions.handler;

import com.canvas.exceptions.UserNotAuthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(UserNotAuthorizedException.class)
    protected ResponseEntity<ErrorResponse> handleUserNotAuthorizedException(
            UserNotAuthorizedException exception
    ) {
        return buildErrorResponse(exception);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(UserNotAuthorizedException exception) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNAUTHORIZED, exception.getMessage());
        return new ResponseEntity<>(errorResponse, errorResponse.getStatus());
    }
}
