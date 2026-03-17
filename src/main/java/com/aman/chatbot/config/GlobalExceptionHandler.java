package com.aman.chatbot.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> handleResponseStatusException(ResponseStatusException ex) {

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(Map.of(
                        "error", ex.getReason()
                ));
    }
}