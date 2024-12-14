package vev.processador_contas.models;

import lombok.*;
import vev.processador_contas.enumerations.TipoPagamento;

import java.time.LocalDate;

@Getter
@Setter
public class Conta {

    private LocalDate data;
    private double valorPagoConta;
    private int codigoConta;

    private TipoPagamento tipoPagamento;

    public Conta(TipoPagamento tipoPagamento, int codigoConta, LocalDate data, double valorTotalConta) {
        this.tipoPagamento = tipoPagamento;
        this.codigoConta = codigoConta;
        this.data = data;
        this.valorPagoConta = valorTotalConta;
    }

    public LocalDate getData() { return this.data; }

    public TipoPagamento getTipoPagamento() { return this.tipoPagamento; }

    public int getCodigoConta() { return this.codigoConta; }

    public double getValorPagoConta() { return this.valorPagoConta; }

    public void setValorPagoConta(double valorTotalConta) { this.valorPagoConta = valorTotalConta; }

    public void setTipoPagamento(TipoPagamento tipoPagamento) { this.tipoPagamento = tipoPagamento; }

    public void setData(LocalDate data) { this.data = data; }
}
