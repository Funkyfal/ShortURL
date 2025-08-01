package org.emobile.urlripper.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "url-config")
public class AppConfig {
    private String baseUrl;
}
