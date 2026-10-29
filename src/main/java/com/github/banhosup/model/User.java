package com.github.banhosup.model;

import java.time.LocalDate;
import java.util.List;

public class User {
    private Integer id;
    private String nome;
    private String email;
    private String telefone;
    private LocalDate dataNascimento;
    private String senha;
    private String senhaRepeticao;
    private List<String> pets;

    public User(){
    }

    public User(Integer id, String username, String nome, String email,
                String telefone, LocalDate dataNascimento) {
        this.id = id;
        this.username = username;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.dataNascimento = dataNascimento;
    }

    public Integer getId() { return id; }

    public void setId(Integer id) { this.id = id; }

    public String getNome() { return nome; }

    public void setNome(String nome) { this.nome = nome; }

    public String getTelefone() { return telefone; }

    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public LocalDate getDataNascimento() { return dataNascimento; }

    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public String getSenhaRepeticao() { return senhaRepeticao; }

    public void setSenhaRepeticao(String senhaRepeticao) { this.senhaRepeticao = senhaRepeticao; }

    public String getSenha() { return senha; }

    public void setSenha(String senha) { this.senha = senha; }

    public List<String> getPets() { return pets; }

    public void setPets(List<String> pets) { this.pets = pets; }
}
