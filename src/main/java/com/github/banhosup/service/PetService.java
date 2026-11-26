package com.github.banhosup.service;

import com.github.banhosup.common.exceptions.BusinessException;
import com.github.banhosup.dto.PetDto;
import com.github.banhosup.model.entities.CustomerEntity;
import com.github.banhosup.model.entities.Pet;
import com.github.banhosup.repository.CustomerRepository;
import com.github.banhosup.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.github.banhosup.repository.appointment.AppointmentsPetOfferingRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PetService {

    private final AppointmentsPetOfferingRepository appointmentsPetOfferingRepository;
    private final PetRepository repository;
    private final CustomerRepository customerRepository;

    // Lista somente os pets do cliente autenticado.
    @Transactional(readOnly = true)
    public List<PetDto> listar(String email) {

        CustomerEntity dono = buscarDonoAutenticado(email);

        return repository.findByDonoId(dono.getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    // Busca um pet somente quando pertence ao cliente autenticado.
    @Transactional(readOnly = true)
    public PetDto buscarPorId(Long id, String email) {

        CustomerEntity dono = buscarDonoAutenticado(email);

        Pet pet = buscarPetDoDono(id, dono);

        return toDto(pet);
    }

    // Cria um pet vinculado ao cliente identificado pelo JWT.
    @Transactional
    public PetDto criar(PetDto dto, String email) {

        CustomerEntity dono = buscarDonoAutenticado(email);

        Pet pet = new Pet();

        preencher(pet, dto);

        // O proprietário vem da autenticação, nunca do JSON.
        pet.setDono(dono);

        return toDto(repository.save(pet));
    }

    // Atualiza somente pets pertencentes ao cliente autenticado.
    @Transactional
    public PetDto atualizar(Long id, PetDto dto, String email) {

        CustomerEntity dono = buscarDonoAutenticado(email);

        Pet pet = buscarPetDoDono(id, dono);

        preencher(pet, dto);

        // Não permite transferir o pet para outro proprietário.
        pet.setDono(dono);

        return toDto(repository.save(pet));
    }

    // Preserva o histórico de agendamentos.
    @Transactional
    public void deletar(Long id, String email) {

        CustomerEntity dono = buscarDonoAutenticado(email);

        Pet pet = buscarPetDoDono(id, dono);

        boolean possuiHistorico =
                appointmentsPetOfferingRepository.existsByPet_Id(id);

        if (possuiHistorico) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "Não é possível excluir este pet porque ele possui histórico de agendamentos."
            );
        }

        repository.delete(pet);
    }

    private Pet buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Pet não encontrado"));
    }

    private CustomerEntity buscarDonoAutenticado(String email) {

        CustomerEntity dono = customerRepository
                .findByUser_Email(email)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.FORBIDDEN,
                        "Apenas clientes podem acessar seus pets."
                ));

        if (!dono.getUser().isActive()) {
            throw new BusinessException(
                    HttpStatus.FORBIDDEN,
                    "Esta conta está inativa."
            );
        }

        return dono;
    }

    private Pet buscarPetDoDono(Long id, CustomerEntity dono) {

        Pet pet = buscarEntidade(id);

        if (!pet.getDono().getId().equals(dono.getId())) {
            throw new BusinessException(
                    HttpStatus.FORBIDDEN,
                    "Você não tem permissão para acessar este pet."
            );
        }

        return pet;
    }

    private void preencher(Pet pet, PetDto dto) {

        pet.setNome(dto.nome());
        pet.setEspecie(dto.especie());
        pet.setRaca(dto.raca());
        pet.setDataNascimento(dto.dataNascimento());
        pet.setPorte(dto.porte());
        pet.setObservacoes(dto.observacoes());
    }

    private PetDto toDto(Pet p) {
        return new PetDto(p.getId(), p.getNome(), p.getEspecie(), p.getRaca(),
                p.getDataNascimento(), p.getPorte(), p.getObservacoes(), p.getDono().getId());
    }
}