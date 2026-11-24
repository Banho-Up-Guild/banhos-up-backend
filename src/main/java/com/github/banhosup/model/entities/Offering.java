package com.github.banhosup.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
@Entity
@Table(name="offerings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Offering {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome; //o nome dos servicos: Banho, Tosa, Banho e tosa...

    @Column(length = 500)
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false)
    private Integer duracaoMinutos;

    @Column(nullable = false)
    private Boolean ativo = true; //desativa o servico sem apaga-lo da coluna


}
