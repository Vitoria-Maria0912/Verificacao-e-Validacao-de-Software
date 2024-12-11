package vev.processador_contas;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
public class Conta {

    private LocalDate data;
    private double valorTotalConta;
    private int codigoConta;

    Conta(int codigoConta, LocalDate data, double valorTotalConta) {
        this.codigoConta = codigoConta;
        this.data = data;
        this.valorTotalConta = valorTotalConta;
    }

    public LocalDate getData() { return this.data; }

    public int getCodigoConta() { return this.codigoConta; }

    public double getValorTotalConta() { return this.valorTotalConta; }

    public void setValorTotalConta(double valorTotalConta) { this.valorTotalConta = valorTotalConta; }
}
