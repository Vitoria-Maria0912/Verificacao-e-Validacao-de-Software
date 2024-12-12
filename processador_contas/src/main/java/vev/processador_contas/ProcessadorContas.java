package vev.processador_contas;

import java.time.LocalDate;
import java.util.*;

public class ProcessadorContas {

    public void processarContas(List<Conta> contas, Fatura fatura) {
        int somaTotalPagamentos = 0;

        if (fatura.getValorTotalFatura() < 0) { System.out.println("A fatura possui valor inválido."); }
        for (Conta conta : contas) {
            if (conta.getValorPagoConta() > 0.01 && conta.getValorPagoConta() < 5000) {
                criarPagamento(conta, fatura);
                somaTotalPagamentos +=
                    (conta.getTipoPagamento() == TipoPagamento.CARTAO_CREDITO && conta.getData().plusDays(-15).isAfter(fatura.getData()))
                    ? 0 : conta.getValorPagoConta();
            }
        }
        System.out.println("Total de pagamentos: " + somaTotalPagamentos);
        System.out.println(fatura.getValorTotalFatura());
        if (somaTotalPagamentos >= fatura.getValorTotalFatura()) {
            fatura.setStatus(FaturaStatus.PAGA);
        }
    }
    public void criarPagamento(Conta conta, Fatura fatura) {
        if (conta.getTipoPagamento() == TipoPagamento.BOLETO && fatura.getData().isAfter(LocalDate.now())) {
            conta.setValorPagoConta(conta.getValorPagoConta() * 1.1);
        }
        fatura.adicionarPagamento(new Pagamento(conta.getTipoPagamento(), LocalDate.now(), conta.getValorPagoConta()));
    }
}
