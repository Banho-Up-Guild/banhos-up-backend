package com.github.banhosup.model.entities;

import java.util.List;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;


@Entity
@Table(name= "tb_pets")
public class PetEntity {
    private String nomePet;
    private String idPet;
    private String racaPet;
    private int idadet;
    private List<String> observacoes;

    public PetEntity(){
    }
    public PetEntity(String nomePet, String idPet, String racaPet, int idadePet) {
        this.nomePet = nomePet;
        this.idPet = iePdPet;
        this.racaPet = racaPet;
        this.idadePet = idadePet;
    }


}
