package com.github.banhosup.service.store;

import com.github.banhosup.common.exceptions.BusinessException;
import com.github.banhosup.dto.store.StoreRequest;
import com.github.banhosup.dto.store.StoreResponse;
import com.github.banhosup.model.entities.store.StoreEntity;
import com.github.banhosup.repository.store.StoreRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository lojaRepository;

    // CADASTRAR LOJA
    @Transactional
    public StoreResponse cadastrar(StoreRequest request) {

        StoreEntity loja = new StoreEntity(
                request.nome().trim(),
                somenteNumeros(request.cep()),
                request.estado().trim().toUpperCase(),
                request.cidade().trim(),
                request.endereco().trim(),
                request.bairro().trim()
        );

        StoreEntity salva = lojaRepository.save(loja);

        return StoreResponse.from(salva);
    }

    // LISTAR LOJAS
    @Transactional(readOnly = true)
    public List<StoreResponse> listar(boolean somenteAtivas) {

        List<StoreEntity> lojas = somenteAtivas
                ? lojaRepository.findByAtivaTrue()
                : lojaRepository.findAll();

        return lojas.stream()
                .map(StoreResponse::from)
                .toList();
    }

    // BUSCAR LOJA PELO ID
    @Transactional(readOnly = true)
    public StoreResponse buscarPorId(Long id) {

        StoreEntity loja = buscarEntidade(id);

        return StoreResponse.from(loja);
    }

    // ATUALIZAR LOJA
    @Transactional
    public StoreResponse atualizar(Long id, StoreRequest request) {

        StoreEntity loja = buscarEntidade(id);

        loja.setNome(request.nome().trim());
        loja.setCep(somenteNumeros(request.cep()));
        loja.setEstado(request.estado().trim().toUpperCase());
        loja.setCidade(request.cidade().trim());
        loja.setEndereco(request.endereco().trim());
        loja.setBairro(request.bairro().trim());

        StoreEntity atualizada = lojaRepository.save(loja);

        return StoreResponse.from(atualizada);
    }

    // ATIVAR OU DESATIVAR LOJA
    @Transactional
    public StoreResponse alterarStatus(Long id, boolean ativa) {

        StoreEntity loja = buscarEntidade(id);

        loja.setAtiva(ativa);

        return StoreResponse.from(lojaRepository.save(loja));
    }

    // MÉTODO AUXILIAR PARA LOCALIZAR UMA LOJA
    private StoreEntity buscarEntidade(Long id) {

        return lojaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "Loja não encontrada. ID: " + id
                ));
    }

    // REMOVE CARACTERES NÃO NUMÉRICOS DO CEP
    private String somenteNumeros(String valor) {
        return valor.replaceAll("\\D", "");
    }
}