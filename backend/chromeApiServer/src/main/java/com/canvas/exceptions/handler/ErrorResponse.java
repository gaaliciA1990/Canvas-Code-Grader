package com.canvas.exceptions.handler;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

@Getter
@Setter
public class ErrorResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private Date timestamp;

    private int code;

    private HttpStatus status;

    private String message;

    public ErrorResponse(HttpStatus httpStatus, String message) {
        this.timestamp = new Date();
        this.status = httpStatus;
        this.code = httpStatus.value();
        this.message = message;
    }
}
