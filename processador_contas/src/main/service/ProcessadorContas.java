package main.service;

import java.time.temporal.ChronoUnit;
import java.time.LocalDate;
import java.util.List;

import main.enumerations.*;
import main.models.*;

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
             valorPagoConta *= (fatura.getData().isBefore(LocalDate.now())) ? 1.1 : 1;
        }
        conta.setValorPagoConta(valorPagoConta);
        fatura.adicionarPagamento(new Pagamento(conta.getTipoPagamento(), LocalDate.now(), valorPagoConta));
    }
}
