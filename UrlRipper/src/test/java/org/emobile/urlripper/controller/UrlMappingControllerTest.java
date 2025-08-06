package org.emobile.urlripper.controller;

import org.emobile.urlripper.dto.OriginalUrlDto;
import org.emobile.urlripper.exception.ShortUrlAlreadyExists;
import org.emobile.urlripper.exception.UrlMappingNotFoundException;
import org.emobile.urlripper.service.UrlMappingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UrlMappingController.class)
class UrlMappingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlMappingService service;

    @Test
    void cut_ValidUrl_ShouldReturnCreated() throws Exception {
        when(service.shorten(new OriginalUrlDto("https://ex.com", null)))
                .thenReturn("http://localhost:8080/ABC1234");

        mockMvc.perform(post("/cut")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"originalUrl\":\"https://ex.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(content().string("http://localhost:8080/ABC1234"));
    }

    @Test
    void cut_ExistingUrl_ShouldReturnConflict() throws Exception {
        when(service.shorten(new OriginalUrlDto("https://ex.com", null)))
                .thenThrow(new ShortUrlAlreadyExists("exists"));

        mockMvc.perform(post("/cut")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"originalUrl\":\"https://ex.com\"}"))
                .andExpect(status().isConflict())
                .andExpect(content().string("exists"));
    }

    @Test
    void redirect_Found_ShouldReturnRedirect() throws Exception {
        when(service.findByShortUrl("AAA"))
                .thenReturn("https://found.com");

        mockMvc.perform(get("/AAA"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://found.com"));
    }

    @Test
    void redirect_NotFound_ShouldReturnNotFound() throws Exception {
        when(service.findByShortUrl("BAD"))
                .thenThrow(new UrlMappingNotFoundException("no"));

        mockMvc.perform(get("/BAD"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("no"));
    }
}
