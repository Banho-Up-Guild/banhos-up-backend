package com.github.banhosup.repository.store;

import com.github.banhosup.model.entities.store.StoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreRepository extends JpaRepository<StoreEntity, Long> {

    List<StoreEntity> findByAtivaTrue();

}