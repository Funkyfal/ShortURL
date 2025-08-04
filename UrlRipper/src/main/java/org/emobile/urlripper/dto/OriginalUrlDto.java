package org.emobile.urlripper.dto;

import java.time.LocalDateTime;

public record OriginalUrlDto(String originalUrl, String alias, String createdBy, LocalDateTime expirationAt){}