package com.canvas.exceptions;

import com.canvas.exceptions.CanvasAPIException;
import com.canvas.exceptions.MakefileNotFoundException;
import com.canvas.exceptions.handler.ControllerExceptionHandler;
import com.canvas.exceptions.handler.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class ExceptionsTests {

    /**
     * Simple test for checking the exception handler is calling the correct methods
     * and returns a response object with no message
     */
    @Test
    public void exceptionHandler_incorrectParams_returns_responseObject_with_no_message(){
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

    /**
     * Simple test for checking the exception handler is calling the correct methods
     * and returns a response object with a message
     */
    @Test
    public void exceptionHandler_incorrectParams_returns_responseObject_with_a_message(){
        // SET UP
        ControllerExceptionHandler handler = new ControllerExceptionHandler();
        String message = "I am a message";
        IncorrectRequestParamsException exception = new IncorrectRequestParamsException(message);

        // ACT
        ResponseEntity<ErrorResponse> response = handler.handleIncorrectRequestParamsExceptions(exception);

        // ASSERT
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, response.getBody().getStatus());
        assertEquals(message, response.getBody().getMessage());
    }

    /**
     * Simple test for checking the exception handler is calling the correct methods
     * and returns a response object with a message
     */
    @Test
    public void exceptionHandler_CanvasAPIException_returns_responseObject_with_no_message(){
        // SET UP
        ControllerExceptionHandler handler = new ControllerExceptionHandler();
        CanvasAPIException exception = new CanvasAPIException();

        // ACT
        ResponseEntity<ErrorResponse> response = handler.handleCanvasAPIException(exception);

        // ASSERT
        assertEquals(HttpStatus.FAILED_DEPENDENCY, response.getStatusCode());
        assertEquals(HttpStatus.FAILED_DEPENDENCY, response.getBody().getStatus());
        assertEquals(null, response.getBody().getMessage());
    }

    /**
     * Simple test for checking the exception handler is calling the correct methods
     * and returns a response object with a message
     */
    @Test
    public void exceptionHandler_CanvasAPIException_returns_responseObject_with_a_message(){
        // SET UP
        ControllerExceptionHandler handler = new ControllerExceptionHandler();
        String message = "I am a message";
        CanvasAPIException exception = new CanvasAPIException(message);

        // ACT
        ResponseEntity<ErrorResponse> response = handler.handleCanvasAPIException(exception);

        // ASSERT
        assertEquals(HttpStatus.FAILED_DEPENDENCY, response.getStatusCode());
        assertEquals(HttpStatus.FAILED_DEPENDENCY, response.getBody().getStatus());
        assertEquals(message, response.getBody().getMessage());
    }

    /**
     * Simple test for checking the exception handler is calling the correct methods
     * and returns a response object with a message
     */
    @Test
    public void exceptionHandler_CanvasAPIException_returns_responseObject_with_a_message_and_exception(){
        // SET UP
        ControllerExceptionHandler handler = new ControllerExceptionHandler();
        String message = "I am a message";
        IOException baseException = new IOException();
        CanvasAPIException exception = new CanvasAPIException(message, baseException);

        // ACT
        ResponseEntity<ErrorResponse> response = handler.handleCanvasAPIException(exception);

        // ASSERT
        assertEquals(HttpStatus.FAILED_DEPENDENCY, response.getStatusCode());
        assertEquals(HttpStatus.FAILED_DEPENDENCY, response.getBody().getStatus());
        assertEquals(message, response.getBody().getMessage());
    }

    @Test
    public void exceptionHandler_handleCanvasAPIException_shouldReturnMakefileNotFoundExceptionErrorResponseWithDefaultMessage() {
        // Arrange
        ControllerExceptionHandler handler = new ControllerExceptionHandler();
        CanvasAPIException exception = new MakefileNotFoundException();

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleCanvasAPIException(exception);

        // Assert
        assertEquals(HttpStatus.FAILED_DEPENDENCY, response.getStatusCode());
        assertEquals(HttpStatus.FAILED_DEPENDENCY, response.getBody().getStatus());
        assertEquals(
                "Makefile was not found in the assignment folder. " +
                        "Please submit a makefile if required to do so. " +
                        "Otherwise, please contact your instructor.",
                response.getBody().getMessage()
        );
    }

    @Test
    public void exceptionHandler_handleCanvasAPIException_shouldReturnMakefileNotFoundExceptionErrorResponseWithCustomMessage() {
        // Arrange
        ControllerExceptionHandler handler = new ControllerExceptionHandler();
        String message = "foo message";
        CanvasAPIException exception = new MakefileNotFoundException(message);

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleCanvasAPIException(exception);

        // Assert
        assertEquals(HttpStatus.FAILED_DEPENDENCY, response.getStatusCode());
        assertEquals(HttpStatus.FAILED_DEPENDENCY, response.getBody().getStatus());
        assertEquals(message, response.getBody().getMessage());
    }

    @Test
    public void exceptionHandler_handleCanvasAPIException_shouldReturnMakefileNotFoundExceptionErrorResponseWithCustomMessageAndException() {
        // Arrange
        ControllerExceptionHandler handler = new ControllerExceptionHandler();
        String message = "foo message";
        CanvasAPIException exception = new MakefileNotFoundException(message, new IOException());

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleCanvasAPIException(exception);

        // Assert
        assertEquals(HttpStatus.FAILED_DEPENDENCY, response.getStatusCode());
        assertEquals(HttpStatus.FAILED_DEPENDENCY, response.getBody().getStatus());
        assertEquals(message, response.getBody().getMessage());
    }

}
