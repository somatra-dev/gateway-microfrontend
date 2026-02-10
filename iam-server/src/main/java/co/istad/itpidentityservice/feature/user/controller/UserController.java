package co.istad.itpidentityservice.feature.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @PreAuthorize("hasAnyAuthority('user:read:own','user:read:all')")
    @GetMapping
    public ResponseEntity<?> findUser(){
        return ResponseEntity.ok(Map.of("message", "Find users successfully"));
    }
}
