package main.models;

import java.time.LocalDate;

import main.enumerations.TipoPagamento;

public class Conta {

    private LocalDate data;
    private double valorPagoConta;
    private int codigoConta;

    private TipoPagamento tipoPagamento;

    public Conta(TipoPagamento tipoPagamento, int codigoConta, LocalDate data, double valorPagoConta) {
        this.tipoPagamento = tipoPagamento;
        this.codigoConta = codigoConta;
        this.data = data;
        this.valorPagoConta = valorPagoConta;
    }

    public LocalDate getData() { return this.data; }

    public TipoPagamento getTipoPagamento() { return this.tipoPagamento; }

    public int getCodigoConta() { return this.codigoConta; }

    public double getValorPagoConta() { return this.valorPagoConta; }

    public void setValorPagoConta(double valorTotalConta) { this.valorPagoConta = valorTotalConta; }

    public void setTipoPagamento(TipoPagamento tipoPagamento) { this.tipoPagamento = tipoPagamento; }

    public void setData(LocalDate data) { this.data = data; }
}
