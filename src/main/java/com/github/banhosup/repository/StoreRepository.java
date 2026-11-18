package com.github.banhosup.repository;

import com.github.banhosup.model.entities.StoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreRepository extends JpaRepository<StoreEntity, Long> {

    List<StoreEntity> findByAtivaTrue();

}