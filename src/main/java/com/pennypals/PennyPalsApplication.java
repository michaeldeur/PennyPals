package com.pennypals;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class PennyPalsApplication {

    @GetMapping("/")
    public String hello() {
        return "Hello World from Penny Pals!";
    }

    public static void main(String[] args) {
        SpringApplication.run(PennyPalsApplication.class, args);
    }
}