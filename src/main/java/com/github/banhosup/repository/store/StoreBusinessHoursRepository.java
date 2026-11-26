package com.github.banhosup.repository.store;

import com.github.banhosup.model.entities.store.StoreBusinessHoursEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreBusinessHoursRepository
        extends JpaRepository<StoreBusinessHoursEntity, Long> {

    List<StoreBusinessHoursEntity>
    findAllByStore_IdOrderByDayOfWeekAsc(Long storeId);
}