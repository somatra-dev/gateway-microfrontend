package com.pesexpo.frontbff.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @GetMapping("/me")
    public Mono<Map<String, Object>> getCurrentUser(
            @AuthenticationPrincipal OidcUser oidcUser,
            @RegisteredOAuth2AuthorizedClient("itp-frontbff") OAuth2AuthorizedClient authorizedClient) {

        Map<String, Object> response = new HashMap<>();

        if (oidcUser == null) {
            response.put("authenticated", false);
            response.put("user", null);
            return Mono.just(response);
        }

        // Build user info from OIDC claims (don't expose raw token)
        Map<String, Object> user = new HashMap<>();
        user.put("sub", oidcUser.getSubject());
        user.put("email", oidcUser.getEmail());
        user.put("name", oidcUser.getFullName());
        user.put("given_name", oidcUser.getGivenName());
        user.put("family_name", oidcUser.getFamilyName());
        user.put("profileImage", oidcUser.getPicture());

        // Get custom claims if present
        if (oidcUser.getClaim("uuid") != null) {
            user.put("uuid", oidcUser.getClaim("uuid"));
        }
        response.put("authenticated", true);
        response.put("user", user);


        return Mono.just(response);
    }


    @GetMapping("/is-authenticated")
    public ResponseEntity<Void> isAuthenticated(Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(401).build();
    }


}

