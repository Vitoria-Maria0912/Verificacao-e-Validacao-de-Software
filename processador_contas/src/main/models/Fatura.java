package main.models;

import java.time.LocalDate;
import java.util.*;

import main.enumerations.FaturaStatus;

public class Fatura {

    private FaturaStatus status;
    private LocalDate data;
    private double valorTotal;
    private String nomeCliente;
    private List<Pagamento> pagamentos;

    public Fatura(LocalDate data, double valorTotal, String nomeCliente) {
        this.data = data;
        this.status = FaturaStatus.PENDENTE;
        this.valorTotal = valorTotal;
        this.nomeCliente = nomeCliente;
        this.pagamentos = new ArrayList<>();
    }

    public FaturaStatus getStatus() { return this.status; }

    public LocalDate getData() { return this.data; }

    public double getValorTotalFatura() { return this.valorTotal; }

    public String getNomeCliente() { return this.nomeCliente; }

    public void setStatus(FaturaStatus faturaStatus) { this.status = faturaStatus; }

    public void setValorTotalFatura(double valorTotalFatura) { this.valorTotal = valorTotalFatura; }

    public void adicionarPagamento(Pagamento pagamento) { this.pagamentos.add(pagamento); }

    public List<Pagamento> getPagamentos() { return this.pagamentos; }
}
