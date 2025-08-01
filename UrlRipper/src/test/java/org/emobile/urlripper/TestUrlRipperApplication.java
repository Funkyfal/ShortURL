package org.emobile.urlripper;

import org.springframework.boot.SpringApplication;

public class TestUrlRipperApplication {

    public static void main(String[] args) {
        SpringApplication.from(UrlRipperApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
