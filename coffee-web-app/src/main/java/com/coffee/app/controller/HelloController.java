package com.coffee.app.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/app")
    @PreAuthorize("hasAuthority('app:member:info')")
    public String index() {
        return "This is app";
    }
}
