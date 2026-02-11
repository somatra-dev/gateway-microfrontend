package com.pesexpo.iphoneservice.service.impl;


import com.pesexpo.iphoneservice.IphoneResponse;
import com.pesexpo.iphoneservice.repository.IphoneRepository;
import com.pesexpo.iphoneservice.service.IphoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IphoneServiceImpl implements IphoneService {

    private final IphoneRepository iphoneRepository;

    @Override
    public List<IphoneResponse> findAll() {

        return iphoneRepository.findAll().stream()
                .map(pro -> IphoneResponse.builder()
                        .productName(pro.getProductName())
                        .productPrice(pro.getProductPrice())
                        .productDescription(pro.getProductDescription())
                        .productImageUrl(pro.getProductImageUrl())
                        .build())
                .toList();
    }
}
