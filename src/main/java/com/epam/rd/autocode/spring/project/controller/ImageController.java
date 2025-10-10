package com.epam.rd.autocode.spring.project.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;

@RestController
public class ImageController {

    @GetMapping("/images/{image}")
    public ResponseEntity<byte[]> getImage(@PathVariable String image) throws IOException {
        Resource resource = new ClassPathResource("static/images/" + image);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        byte[] bytes = Files.readAllBytes(resource.getFile().toPath());
        return ResponseEntity.ok()
                .header("Content-Type", "image/png")
                .body(bytes);
    }
}
