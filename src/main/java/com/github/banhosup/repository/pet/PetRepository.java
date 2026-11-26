package com.github.banhosup.repository.pet;

import com.github.banhosup.model.entities.pet.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PetRepository extends JpaRepository<Pet, Long> {

    List<Pet> findByDonoId(Long donoId);
}