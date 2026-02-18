package com.pesexpo.bffgateway.controller;


import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class GatewayFallbackController {

    @GetMapping("/iphone-api")
    public Mono<ResponseEntity<Map<String, Object>>> iphoneApiFallback() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        body.put("error", "Service Unavailable");
        body.put("message", "iphone-service is temporarily unavailable. please try retry again");
        body.put("path", "/api/v1/iphones");

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body));
    }

    @GetMapping("/iphone")
    public Mono<ResponseEntity<Void>> iphoneFallback() {
        return redirectToUiFallback("iphone");
    }

    @GetMapping("/ipad")
    public Mono<ResponseEntity<Void>> ipadFallback() {
        return redirectToUiFallback("ipad");
    }

    @GetMapping("/macbook")
    public Mono<ResponseEntity<Void>> macbookFallback() {
        return redirectToUiFallback("macbook");
    }

    private Mono<ResponseEntity<Void>> redirectToUiFallback(String zone) {
        return Mono.just(
                ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
                        .header(HttpHeaders.LOCATION, "/fallback?zone=" + zone)
                        .build()
        );
    }
}
