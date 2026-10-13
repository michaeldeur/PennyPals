package com.pennypals;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
@Controller
public class PennyPalsApplication {

    @GetMapping("/")
    public String index() {
        return "index"; // Looks for src/main/resources/templates/index.html
    }

    public static void main(String[] args) {
        SpringApplication.run(PennyPalsApplication.class, args);
    }
}