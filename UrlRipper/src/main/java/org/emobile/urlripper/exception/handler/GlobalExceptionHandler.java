package org.emobile.urlripper.exception.handler;

import org.emobile.urlripper.exception.AliasDoesntMatch;
import org.emobile.urlripper.exception.ShortUrlAlreadyExists;
import org.emobile.urlripper.exception.UrlMappingNotFoundException;
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
}
