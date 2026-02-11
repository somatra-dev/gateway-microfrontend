package com.pesexpo.iphoneservice.service;

import com.pesexpo.iphoneservice.IphoneResponse;
import com.pesexpo.iphoneservice.domain.IphoneProduct;

import java.util.List;

public interface IphoneService {

    List<IphoneResponse> findAll();

}
