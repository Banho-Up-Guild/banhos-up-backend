package com.github.banhosup.repository;

import com.github.banhosup.model.entities.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PetRepository extends JpaRepository<Pet, Long> {

    List<Pet> findByDonoId(Long donoId);
}