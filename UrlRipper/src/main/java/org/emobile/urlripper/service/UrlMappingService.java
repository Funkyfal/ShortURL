package org.emobile.urlripper.service;

import lombok.AllArgsConstructor;
import org.emobile.urlripper.configuration.AppConfig;
import org.emobile.urlripper.entity.UrlMapping;
import org.emobile.urlripper.exception.ShortUrlAlreadyExists;
import org.emobile.urlripper.exception.UrlMappingNotFoundException;
import org.emobile.urlripper.repository.UrlMappingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

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
    public String shorten(String originalUrl) {
        urlMappingRepository.findByOriginalUrl(originalUrl)
                .ifPresent(mapping -> {
                            throw new ShortUrlAlreadyExists(mapping.getShortCode());
                        }
                );

        String shortCode;
        do {
            shortCode = generateRandomBase62();
        } while (urlMappingRepository.existsByShortCode(shortCode));

        UrlMapping urlMapping = UrlMapping.builder()
                .originalUrl(originalUrl)
                .shortCode(shortCode)
                .build();
        urlMappingRepository.save(urlMapping);

        return String.format("%s/%s", appConfig.getBaseUrl(), shortCode);
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
