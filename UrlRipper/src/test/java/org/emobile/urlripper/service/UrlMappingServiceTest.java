package org.emobile.urlripper.service;

import org.emobile.urlripper.configuration.AppConfig;
import org.emobile.urlripper.entity.UrlMapping;
import org.emobile.urlripper.exception.ShortUrlAlreadyExists;
import org.emobile.urlripper.exception.UrlMappingNotFoundException;
import org.emobile.urlripper.repository.UrlMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UrlMappingServiceTest {

    @Mock
    private UrlMappingRepository repository;
    @Mock
    private AppConfig appConfig;

    @InjectMocks
    private UrlMappingService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(appConfig.getBaseUrl()).thenReturn("http://localhost:8080");
    }

    @Test
    void shorten_NewUrl_ShouldSaveAndReturnShortLink() {
        String original = "https://example.com/path";
        when(repository.findByOriginalUrl(original)).thenReturn(Optional.empty());
        when(repository.existsByShortCode(anyString())).thenReturn(false);

        String result = service.shorten(original);

        assertThat(result).startsWith("http://localhost:8080/");
        String code = result.substring(result.lastIndexOf('/') + 1);
        assertThat(code).hasSize(7);
        assertThat(Pattern.matches("[0-9a-zA-Z]{7}", code)).isTrue();

        ArgumentCaptor<UrlMapping> captor = ArgumentCaptor.forClass(UrlMapping.class);
        verify(repository).save(captor.capture());
        UrlMapping saved = captor.getValue();
        assertThat(saved.getOriginalUrl()).isEqualTo(original);
        assertThat(saved.getShortCode()).isEqualTo(code);
    }

    @Test
    void shorten_ExistingOriginal_ShouldThrow() {
        String original = "https://example.com/dup";
        UrlMapping existing = new UrlMapping();
        existing.setShortCode("ABC1234");
        when(repository.findByOriginalUrl(original)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.shorten(original))
                .isInstanceOf(ShortUrlAlreadyExists.class)
                .hasMessageContaining("ABC1234");
        verify(repository, never()).save(any());
    }

    @Test
    void findByShortUrl_NotFound_ShouldThrow() {
        when(repository.findByShortCode("XYZ")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findByShortUrl("XYZ"))
                .isInstanceOf(UrlMappingNotFoundException.class)
                .hasMessageContaining("XYZ");
    }

    @Test
    void findByShortUrl_Found_ShouldReturnOriginal() {
        UrlMapping mapping = new UrlMapping();
        mapping.setOriginalUrl("https://found.com");
        when(repository.findByShortCode("AAA1111")).thenReturn(Optional.of(mapping));

        String result = service.findByShortUrl("AAA1111");
        assertThat(result).isEqualTo("https://found.com");
    }
}