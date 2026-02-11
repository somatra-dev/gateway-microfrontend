package com.pesexpo.iphoneservice.controller;


import com.pesexpo.iphoneservice.IphoneResponse;
import com.pesexpo.iphoneservice.service.IphoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/iphones")
@RequiredArgsConstructor
public class IphoneController {

    private final IphoneService iphoneService;

    @GetMapping
    public ResponseEntity<List<IphoneResponse>> getIphone() {
        return ResponseEntity.ok().body(iphoneService.findAll());
    }

}
