package vev.processador_contas;

import java.time.LocalDate;

public class Conta {

    private LocalDate data;
    private double valorTotal;

    Conta(LocalDate data, double valorTotal) {
        this.data = data;
        this.valorTotal = valorTotal;
    }

    public LocalDate getData() { return this.data; }

    public double getValorTotal() { return this.valorTotal; }
}
