package com.pesexpo.iphoneservice;

import lombok.Builder;

@Builder
public record IphoneResponse (

        String productName,

        Double productPrice,

        String productDescription,

        String productImageUrl
) {
}
