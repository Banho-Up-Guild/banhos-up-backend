package com.github.banhosup.model.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lojas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false, length = 100)
    private String nome;

    @Setter
    @Column(nullable = false, length = 8)
    private String cep;

    @Setter
    @Column(nullable = false, length = 2)
    private String estado;

    @Setter
    @Column(nullable = false, length = 100)
    private String cidade;

    @Setter
    @Column(nullable = false, length = 150)
    private String endereco;

    @Setter
    @Column(nullable = false, length = 100)
    private String bairro;

    @Setter
    @Column(nullable = false)
    private boolean ativa = true;

    public StoreEntity(
            String nome,
            String cep,
            String estado,
            String cidade,
            String endereco,
            String bairro
    ) {
        this.nome = nome;
        this.cep = cep;
        this.estado = estado;
        this.cidade = cidade;
        this.endereco = endereco;
        this.bairro = bairro;
        this.ativa = true;
    }
}