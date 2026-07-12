package com.horseracing.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    @GetMapping("/")
    public String root() {
        return "Horse Racing API is running";
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
