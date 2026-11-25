package com.github.banhosup.service;

import com.github.banhosup.common.exceptions.BusinessException;
import com.github.banhosup.dto.OfferingDto;
import com.github.banhosup.model.entities.Offering;
import com.github.banhosup.repository.OfferingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OfferingService {

    private final OfferingRepository repository;

    @Transactional(readOnly = true)
    public List<OfferingDto> listar(boolean apenasAtivos) {
        List<Offering> Offerings = apenasAtivos ? repository.findByAtivoTrue() : repository.findAll();
        return Offerings.stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public OfferingDto buscarPorId(Long id) {
        return toDto(buscarEntidade(id));
    }

    @Transactional
    public OfferingDto criar(OfferingDto dto) {
        if (repository.existsByNomeIgnoreCase(dto.nome())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Já existe um serviço com este nome");
        }
        Offering Offering = new Offering();
        preencher(Offering, dto);
        return toDto(repository.save(Offering));
    }

    @Transactional
    public OfferingDto atualizar(Long id, OfferingDto dto) {
        Offering Offering = buscarEntidade(id);
        if (repository.existsByNomeIgnoreCaseAndIdNot(dto.nome(), id)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Já existe um serviço com este nome");
        }
        preencher(Offering, dto);
        return toDto(repository.save(Offering));
    }

    @Transactional
    public void deletar(Long id) {

        Offering offering = buscarEntidade(id);

        // Desativa o serviço sem remover seu histórico.
        offering.setAtivo(false);

        repository.save(offering);
    }

    private Offering buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Serviço não encontrado"));
    }

    private void preencher(Offering Offering, OfferingDto dto) {
        Offering.setNome(dto.nome());
        Offering.setDescricao(dto.descricao());
        Offering.setPreco(dto.preco());
        Offering.setDuracaoMinutos(dto.duracaoMinutos());
        // se não vier "ativo" no JSON, mantém o valor atual (novo serviço começa como true)
        if (dto.ativo() != null) {
            Offering.setAtivo(dto.ativo());
        }
    }

    private OfferingDto toDto(Offering s) {
        return new OfferingDto(s.getId(), s.getNome(), s.getDescricao(),
                s.getPreco(), s.getDuracaoMinutos(), s.getAtivo());
    }
}