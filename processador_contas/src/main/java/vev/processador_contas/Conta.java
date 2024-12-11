package vev.processador_contas;

import lombok.*;

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

    public int getCodigoConta() { return this.codigoConta; }

    public double getValorPagoConta() { return this.valorPagoConta; }

    public void setValorPagoConta(double valorTotalConta) { this.valorPagoConta = valorTotalConta; }
}
