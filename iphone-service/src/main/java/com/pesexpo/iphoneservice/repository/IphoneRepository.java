package com.pesexpo.iphoneservice.repository;

import com.pesexpo.iphoneservice.domain.IphoneProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IphoneRepository extends JpaRepository<IphoneProduct, Long> {
}
