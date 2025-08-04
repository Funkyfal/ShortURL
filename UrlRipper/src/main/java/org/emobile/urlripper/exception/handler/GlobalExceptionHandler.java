package org.emobile.urlripper.exception.handler;

import org.emobile.urlripper.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UrlMappingNotFoundException.class)
    public ResponseEntity<String> handleUrlMappingNotFoundException(UrlMappingNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    @ExceptionHandler(ShortUrlAlreadyExists.class)
    public ResponseEntity<String> handleShortUrlAlreadyExists(ShortUrlAlreadyExists ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ex.getMessage());
    }

    @ExceptionHandler(AliasDoesntMatch.class)
    public ResponseEntity<String> handleAliasDoesntMatch(AliasDoesntMatch ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    @ExceptionHandler(CodeGenerationException.class)
    public ResponseEntity<String> handleCodeGenerationException(CodeGenerationException ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ex.getMessage());
    }

    @ExceptionHandler(InvalidUrlException.class)
    public ResponseEntity<String> handleInvalidUrlException(InvalidUrlException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    @ExceptionHandler(UrlExpiredException.class)
    public ResponseEntity<String> handleUrlExpiredException(UrlExpiredException ex) {
        return ResponseEntity
                .status(HttpStatus.GONE)
                .body(ex.getMessage());
    }
}
