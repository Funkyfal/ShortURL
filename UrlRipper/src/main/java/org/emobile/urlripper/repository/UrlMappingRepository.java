package org.emobile.urlripper.repository;

import org.emobile.urlripper.entity.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {
    Optional<UrlMapping> findByShortCode(String shortUrl);
    Optional<UrlMapping> findByOriginalUrl(String url);
    boolean existsByShortCode(String shortUrl);
}
