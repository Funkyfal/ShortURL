package org.emobile.urlripper.controller;

import lombok.AllArgsConstructor;
import org.emobile.urlripper.dto.OriginalUrlDto;
import org.emobile.urlripper.service.UrlMappingService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;


@RestController
@AllArgsConstructor
public class UrlMappingController {
    private final UrlMappingService urlMappingService;

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        String originalUrl = urlMappingService.findByShortUrl(shortCode);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(originalUrl));

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @PostMapping("/cut")
    public ResponseEntity<String> cut(@RequestBody OriginalUrlDto originalUrlDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(urlMappingService.shorten(originalUrlDto.originalUrl()));
    }
}
