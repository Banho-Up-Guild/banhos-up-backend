package com.github.banhosup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.github.banhosup.model.entities.CustomerEntity;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {
}
