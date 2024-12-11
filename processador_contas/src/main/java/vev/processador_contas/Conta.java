package vev.processador_contas;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
public class Conta {

    private LocalDate data;
    private double valorTotal;

    Conta(LocalDate data, double valorTotal) {
        this.data = data;
        this.valorTotal = valorTotal;
    }

    public LocalDate getData() { return this.data; }

    public double getValorTotalConta() { return this.valorTotal; }

    public void setValorTotalConta(double valorTotal) { this.valorTotal = valorTotal; }
}
