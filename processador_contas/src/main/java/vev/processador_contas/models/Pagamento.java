package vev.processador_contas;

import lombok.*;
import vev.processador_contas.enumerations.TipoPagamento;

import java.time.LocalDate;

@Getter
@Setter
public class Pagamento {

    private TipoPagamento tipoPagamento;
    private double valorPago;
    private LocalDate data;

    public Pagamento(TipoPagamento tipoPagamento, LocalDate data, double valorPago) {
        this.data = data;
        this.valorPago = valorPago;
        this.tipoPagamento = tipoPagamento;
    }

    public double getValorPago() { return this.valorPago; }

    public LocalDate getData() { return this.data; }
}
