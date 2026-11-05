package com.github.banhosup.model.entities;

import java.time.LocalDate;


public class Servico {
    private String idServico;
    private String nomeServico;
    private double valorServico;
    private String descricaoServico;
    private int duracaoServico;
    private boolean ativoServico;

    public Servico(){
    }
    public Servico(String idServico, String nomeServico, double valorServico, String descricaoServico,
                   int duracaoServico, boolean ativoServico){
        this.idServico = idServico;
        this.nomeServico = nomeServico;
        this.valorServico = valorServico;
        this.descricaoServico = descricaoServico;
        this.ativoServico = ativoServico;
    }

    public String getIdServico() { return idServico; }

    public void setIdServico(String idServico) { this.idServico = idServico; }

    public String getNomeServico() { return nomeServico; }

    public void setNomeServico(String nomeServico) { this.nomeServico = nomeServico; }

    public double getValorServico() { return valorServico; }

    public void setValorServico(double valorServico) { this.valorServico = valorServico; }

    public int getDuracaoServico() { return duracaoServico; }

    public void setDuracaoServico(int duracaoServico) { this.duracaoServico = duracaoServico; }

    public String getDescricaoServico() { return descricaoServico; }

    public void setDescricaoServico(String descricaoServico) { this.descricaoServico = descricaoServico; }

    public boolean isAtivoServico() { return ativoServico; }

    public void setAtivoServico(boolean ativoServico) { this.ativoServico = ativoServico; }
}
