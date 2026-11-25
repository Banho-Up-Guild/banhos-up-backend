package com.github.banhosup.service;

import com.github.banhosup.common.exceptions.BusinessException;
import com.github.banhosup.dto.pet.PetDto;
import com.github.banhosup.model.entities.CustomerEntity;
import com.github.banhosup.model.entities.Pet;
import com.github.banhosup.repository.CustomerRepository;
import com.github.banhosup.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository repository;
    private final CustomerRepository customerRepository;

    // ID de dono opcional caso apareça necessidade
    @Transactional(readOnly = true)
    public List<PetDto> listar(Long donoId) {
        List<Pet> pets = (donoId == null) ? repository.findAll() : repository.findByDonoId(donoId);
        return pets.stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public PetDto buscarPorId(Long id) {
        return toDto(buscarEntidade(id));
    }

    @Transactional
    public PetDto criar(PetDto dto) {
        Pet pet = new Pet();
        preencher(pet, dto);
        return toDto(repository.save(pet));
    }

    @Transactional
    public PetDto atualizar(Long id, PetDto dto) {
        Pet pet = buscarEntidade(id);
        preencher(pet, dto);
        return toDto(repository.save(pet));
    }

    @Transactional
    public void deletar(Long id) {
        repository.delete(buscarEntidade(id));
    }

    private Pet buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Pet não encontrado"));
    }

    private void preencher(Pet pet, PetDto dto) {
        CustomerEntity dono = customerRepository.findById(dto.donoId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Dono não encontrado"));
        pet.setNome(dto.nome());
        pet.setEspecie(dto.especie());
        pet.setRaca(dto.raca());
        pet.setDataNascimento(dto.dataNascimento());
        pet.setPorte(dto.porte());
        pet.setObservacoes(dto.observacoes());
        pet.setDono(dono);
    }

    private PetDto toDto(Pet p) {
        return new PetDto(p.getId(), p.getNome(), p.getEspecie(), p.getRaca(),
                p.getDataNascimento(), p.getPorte(), p.getObservacoes(), p.getDono().getId());
    }
}