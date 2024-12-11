package vev.processador_contas;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
public class Fatura {

    private FaturaStatus status;
    private LocalDate data;
    private double valorTotal;
    private String nomeCliente;

    public Fatura(LocalDate data, double valorTotal, String nomeCliente) {
        this.data = data;
        this.status = FaturaStatus.PENDENTE;
        this.valorTotal = valorTotal;
        this.nomeCliente = nomeCliente;
    }

    public FaturaStatus getStatus() { return this.status; }

    public LocalDate getData() { return this.data; }

    public double getValorTotalFatura() { return this.valorTotal; }

    public String getNomeCliente() { return this.nomeCliente; }

    public void setStatus(FaturaStatus faturaStatus) { this.status = faturaStatus; }

    public void setValorTotalFatura(double valorTotalFatura) { this.valorTotal = valorTotalFatura; }
}
