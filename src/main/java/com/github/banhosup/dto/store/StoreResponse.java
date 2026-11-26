package com.github.banhosup.dto.store;

import com.github.banhosup.model.entities.store.StoreEntity;

public record StoreResponse(

        Long id,
        String nome,
        String cep,
        String estado,
        String cidade,
        String endereco,
        String bairro,
        boolean ativa

) {

    public static StoreResponse from(StoreEntity loja) {

        return new StoreResponse(
                loja.getId(),
                loja.getNome(),
                loja.getCep(),
                loja.getEstado(),
                loja.getCidade(),
                loja.getEndereco(),
                loja.getBairro(),
                loja.isAtiva()
        );
    }
}
