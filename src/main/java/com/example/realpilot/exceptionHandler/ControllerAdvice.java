package com.example.realpilot.exceptionHandler;

import com.example.realpilot.exceptionList.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ControllerAdvice extends ResponseEntityExceptionHandler {
    @ExceptionHandler(DgraphQueryException.class)
    public ResponseEntity handleDgraphQueryException(DgraphQueryException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    @ExceptionHandler(JsonAndObjectMappingException.class)
    public ResponseEntity handleJsonAndObjectMappingException(JsonAndObjectMappingException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    @ExceptionHandler(ExcelFileIOException.class)
    public ResponseEntity handleExcelFileIOException(ExcelFileIOException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    @ExceptionHandler(ExcelFileNotFoundException.class)
    public ResponseEntity handleExcelFileNotFoundException(ExcelFileNotFoundException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    @ExceptionHandler(ApiCallException.class)
    public ResponseEntity handleApiCallException(ApiCallException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }


    // BAD_REQUEST, FORBIDDEN, NOT_FOUND, SERVICE_UNAVAILABLE, ...
}
