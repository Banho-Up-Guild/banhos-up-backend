package com.github.banhosup.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "servicos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Servicos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome; //aqui ficarão os nomes dos serviços, Banho, Tosa, banho e tosa e tal

    @Column(length = 500)
    private String descricao; //a descricao do dito serviço escolhido na tabela

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco; // valor de cada serviço oferecido

    @Column(nullable = false)
    private Integer duracaoMinutos; //duracao minima de cada servico em minutos (ex: Banho - 25 min)

    @Column(nullable = false)
    private Boolean ativo = true; // desativa o servico sem apagá-lo da tabela
}
