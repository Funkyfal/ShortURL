package org.emobile.urlripper.service;

import lombok.AllArgsConstructor;
import org.emobile.urlripper.configuration.AppConfig;
import org.emobile.urlripper.dto.OriginalUrlDto;
import org.emobile.urlripper.entity.UrlMapping;
import org.emobile.urlripper.exception.AliasDoesntMatch;
import org.emobile.urlripper.exception.ShortUrlAlreadyExists;
import org.emobile.urlripper.exception.UrlMappingNotFoundException;
import org.emobile.urlripper.repository.UrlMappingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UrlMappingService {
    private static final int SHORT_CODE_LENGTH = 7;

    private final SecureRandom random = new SecureRandom();
    private final String BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final AppConfig appConfig;
    private final UrlMappingRepository urlMappingRepository;

    public String findByShortUrl(String shortCode) {
        return urlMappingRepository.findByShortCode(shortCode)
                .map(UrlMapping::getOriginalUrl)
                .orElseThrow(() -> new UrlMappingNotFoundException("No original URL with short code: " + shortCode));
    }

    @Transactional
    public String shorten(OriginalUrlDto originalUrlDto) {
        urlMappingRepository.findByOriginalUrl(originalUrlDto.originalUrl())
                .ifPresent(mapping -> {
                            throw new ShortUrlAlreadyExists(mapping.getShortCode());
                        }
                );

        String shortCode = Optional.ofNullable(originalUrlDto.alias())
                .filter(this::hasText)
                .map(this::validateAliasOrThrow)
                .orElseGet(this::generateUniqueCode);

        UrlMapping urlMapping = UrlMapping.builder()
                .originalUrl(originalUrlDto.originalUrl())
                .shortCode(shortCode)
                .build();
        urlMappingRepository.save(urlMapping);

        return String.format("%s/%s", appConfig.getBaseUrl(), shortCode);
    }

    private boolean hasText(String s) {
        return s != null && !s.isEmpty();
    }

    private String validateAliasOrThrow(String alias) {
        if (!alias.matches("^[0-9A-Za-z]+$")) {
            throw new AliasDoesntMatch("Alias must match [0-9A-Za-z]+");
        }
        if (urlMappingRepository.existsByShortCode(alias)) {
            throw new ShortUrlAlreadyExists("Alias already exists: "  + alias);
        }
        return alias;
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = generateRandomBase62();
        } while (urlMappingRepository.existsByShortCode(code));
        return code;
    }

    private String generateRandomBase62() {
        StringBuilder sb = new StringBuilder(UrlMappingService.SHORT_CODE_LENGTH);
        for (int i = 0; i < UrlMappingService.SHORT_CODE_LENGTH; i++) {
            int idx = random.nextInt(BASE62.length());
            sb.append(BASE62.charAt(idx));
        }
        return sb.toString();
    }
}
