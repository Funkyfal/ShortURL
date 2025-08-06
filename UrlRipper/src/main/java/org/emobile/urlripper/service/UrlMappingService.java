package org.emobile.urlripper.service;

import lombok.AllArgsConstructor;
import org.emobile.urlripper.configuration.AppConfig;
import org.emobile.urlripper.dto.OriginalUrlDto;
import org.emobile.urlripper.entity.UrlMapping;
import org.emobile.urlripper.exception.*;
import org.emobile.urlripper.repository.UrlMappingRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UrlMappingService {
    private static final int SHORT_CODE_LENGTH = 7;
    private static final int MAX_GENERATION_ATTEMPTS = 5;

    private final SecureRandom random = new SecureRandom();
    private final String BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final AppConfig appConfig;
    private final UrlMappingRepository urlMappingRepository;

    @Cacheable(cacheNames = "codeToUrl", key = "#shortCode")
    public String findByShortUrl(String shortCode) {
        UrlMapping mapping = urlMappingRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlMappingNotFoundException(shortCode));

        LocalDateTime expires = mapping.getExpirationAt();
        if (expires != null && LocalDateTime.now().isAfter(expires)) {
            throw new UrlExpiredException(shortCode);
        }

        return mapping.getOriginalUrl();
    }

    @Transactional
    @Cacheable(cacheNames = "urlToCode", key = "#originalUrlDto.originalUrl")
    public String shorten(OriginalUrlDto originalUrlDto) {
        validateUrlOrThrow(originalUrlDto.originalUrl());

        urlMappingRepository.findByOriginalUrl(originalUrlDto.originalUrl())
                .ifPresent(mapping -> {
                            throw new ShortUrlAlreadyExists("Url with shortcode: " + mapping.getShortCode() + " already exists");
                        }
                );

        String shortCode = Optional.ofNullable(originalUrlDto.alias())
                .filter(StringUtils::hasText)
                .map(this::validateAliasOrThrow)
                .orElseGet(this::generateUniqueCode);

        LocalDateTime expirationAt = Optional.ofNullable(originalUrlDto.expirationAt())
                .orElse(LocalDateTime.now().plusDays(1));

        String createdBy = Optional.ofNullable(originalUrlDto.createdBy())
                .orElse("unknown");

        UrlMapping urlMapping = UrlMapping.builder()
                .originalUrl(originalUrlDto.originalUrl())
                .shortCode(shortCode)
                .expirationAt(expirationAt)
                .createdBy(createdBy)
                .build();
        urlMappingRepository.save(urlMapping);

        return String.format("%s/%s", appConfig.getBaseUrl(), shortCode);
    }

    private String validateAliasOrThrow(String alias) {
        if (!alias.matches("^[0-9A-Za-z]+$")) {
            throw new AliasDoesntMatch("Alias must match [0-9A-Za-z]+");
        }
        if (urlMappingRepository.existsByShortCode(alias)) {
            throw new ShortUrlAlreadyExists("Alias already exists: " + alias);
        }
        return alias;
    }

    private String generateUniqueCode() {
        String code;
        for (int i = 0; i < MAX_GENERATION_ATTEMPTS; i++) {
            code = generateRandomBase62();
            if (!urlMappingRepository.existsByShortCode(code)) {
                return code;
            }
        }
        throw new CodeGenerationException(
                "Failed to generate code in " + MAX_GENERATION_ATTEMPTS + " attempts"
        );
    }

    private String generateRandomBase62() {
        StringBuilder sb = new StringBuilder(UrlMappingService.SHORT_CODE_LENGTH);
        for (int i = 0; i < UrlMappingService.SHORT_CODE_LENGTH; i++) {
            int idx = random.nextInt(BASE62.length());
            sb.append(BASE62.charAt(idx));
        }
        return sb.toString();
    }

    private void validateUrlOrThrow(String url) {
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            String host   = uri.getHost();
            if (scheme == null || (!scheme.equals("http") && !scheme.equals("https"))
                    || host == null || host.isBlank()) {
                throw new InvalidUrlException("Invalid URL: " + url);
            }
        } catch (URISyntaxException e) {
            throw new InvalidUrlException("Invalid URL: " + url);
        }
    }
}
