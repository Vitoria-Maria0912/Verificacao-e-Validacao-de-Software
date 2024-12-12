package vev.processador_contas;

import vev.processador_contas.enumerations.FaturaStatus;
import vev.processador_contas.enumerations.TipoPagamento;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class ProcessadorContas {

    public void processarContas(List<Conta> contas, Fatura fatura) {
        int somaTotalPagamentos = 0;

        if (fatura.getValorTotalFatura() < 0) { return; }
        for (Conta conta : contas) {
            long diferencaDias = ChronoUnit.DAYS.between(conta.getData(), fatura.getData());
            somaTotalPagamentos +=
                    ((conta.getTipoPagamento() == TipoPagamento.CARTAO_CREDITO) && (diferencaDias >= 15 || diferencaDias == 0)) ? conta.getValorPagoConta()
                    : (conta.getTipoPagamento() != TipoPagamento.CARTAO_CREDITO) ? conta.getValorPagoConta()
                    : 0;
            criarPagamento(conta, fatura);
        }
        if (somaTotalPagamentos >= fatura.getValorTotalFatura()) {
            fatura.setStatus(FaturaStatus.PAGA);
        }
    }
    public void criarPagamento(Conta conta, Fatura fatura) {
        double valorPagoConta = conta.getValorPagoConta();
        if (conta.getTipoPagamento() == TipoPagamento.BOLETO && conta.getValorPagoConta() > 0.01 && conta.getValorPagoConta() < 5000) {
             valorPagoConta *= (fatura.getData().isAfter(LocalDate.now())) ? 1.1 : 1;
        }
        fatura.adicionarPagamento(new Pagamento(conta.getTipoPagamento(), LocalDate.now(), valorPagoConta));
    }
}
