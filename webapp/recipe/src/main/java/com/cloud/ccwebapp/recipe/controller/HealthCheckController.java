package com.cloud.ccwebapp.recipe.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @GetMapping("/healthstatus")
    public ResponseEntity checkStatus() {
        return new ResponseEntity(HttpStatus.OK);
    }
}
