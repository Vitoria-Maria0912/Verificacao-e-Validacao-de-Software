package vev.processador_contas;

import lombok.*;
import java.util.*;

@Getter
@Setter
public class ProcessadorContas {

    private List<Conta> contas;
    private Fatura fatura;

    public ProcessadorContas(List<Conta> contas, Fatura fatura) {
        this.contas = contas;
        this.fatura = fatura;
    }
    public void processarContas(List<Conta> contas, Fatura fatura) {
        for (Conta conta : contas) {
            if (conta.getValorPagoConta() >= fatura.getValorTotalFatura()) {
                fatura.setStatus(FaturaStatus.PAGA);
            }
        }
    }

    public List<Conta> getContas() { return this.contas; }

    public Fatura getFatura() { return this.fatura; }
}
