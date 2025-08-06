package org.emobile.urlripper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class UrlRipperApplication {

    public static void main(String[] args) {
        SpringApplication.run(UrlRipperApplication.class, args);
    }

}
