package vev.processador_contas;

import lombok.*;

import java.time.LocalDate;
import java.util.*;

@Getter
@Setter
public class ProcessadorContas {

    public void processarContas(List<Conta> contas, Fatura fatura) {
        int somaTotalPagamentos = 0;
        for (Conta conta : contas) {
            if (conta.getValorPagoConta() >= fatura.getValorTotalFatura()) {
                criarPagamento(conta, fatura);
            }
            somaTotalPagamentos += conta.getValorPagoConta();
        }
    }
    public void criarPagamento(Conta conta, Fatura fatura) {
        if (conta.getTipoPagamento() == TipoPagamento.BOLETO && fatura.getData().isAfter(LocalDate.now())) {
            fatura.setValorTotalFatura(fatura.getValorTotalFatura() * 1.1);
        }
        new Pagamento(conta.getTipoPagamento(), LocalDate.now(), conta.getValorPagoConta());
        fatura.setStatus(FaturaStatus.PAGA);
    }
}
