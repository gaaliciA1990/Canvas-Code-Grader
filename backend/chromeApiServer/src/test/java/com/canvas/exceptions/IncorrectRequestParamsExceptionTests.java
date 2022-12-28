package com.canvas.exceptions;

import com.canvas.exceptions.handler.ControllerExceptionHandler;
import com.canvas.exceptions.handler.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class IncorrectRequestParamsExceptionTests {

    /**
     * Simple test for checking the exception handler is calling the correct methods
     * and returns a JSON response and https status
     */
    @Test
    public void exceptionHandler_incorrectParams_returns_responseObject(){
        // SET UP
        ControllerExceptionHandler handler = new ControllerExceptionHandler();
        IncorrectRequestParamsException exception = new IncorrectRequestParamsException();

        // ACT
        ResponseEntity<ErrorResponse> response = handler.handleIncorrectRequestParamsExceptions(exception);

        // ASSERT
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, response.getBody().getStatus());
        assertEquals(null, response.getBody().getMessage());
    }
}
