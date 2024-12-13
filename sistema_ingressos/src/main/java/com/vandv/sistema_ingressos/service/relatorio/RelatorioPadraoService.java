package com.vandv.sistema_ingressos.service.relatorio;

import com.vandv.sistema_ingressos.exception.Show.IdInvalidoException;
import com.vandv.sistema_ingressos.model.*;
import com.vandv.sistema_ingressos.repository.ShowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RelatorioPadraoService implements RelatorioService {
    @Autowired
    ShowRepository showRepository;
    public RelatorioShow geraRelatorio(Long idShow) {
        Show show = showRepository.findById(idShow).orElseThrow(IdInvalidoException::new);

        int ingressosNormais = 0;
        int ingressosVIP = 0;
        int ingressosMeiaEntrada = 0;
        double receitaBruta = 0.0;

        // Contar ingressos vendidos e calcular receita
        for (Lote lote : show.getLote()) {
            for (Ingresso ingresso : lote.getIngressos()) {
                if (ingresso.getStatus() == StatusIngresso.VENDIDO) {
                    receitaBruta += ingresso.getPreco();

                    switch (ingresso.getTipo()) {
                        case NORMAL:
                            ingressosNormais++;
                            break;
                        case VIP:
                            ingressosVIP++;
                            break;
                        case MEIA_ENTRADA:
                            ingressosMeiaEntrada++;
                            break;
                    }
                }
            }
        }

        // Calcular despesas de infraestrutura com acréscimo se a data for especial
        double despesasInfraestrutura = show.getTotalDespesas();
        if (show.isDataEspecial()) {
            despesasInfraestrutura += despesasInfraestrutura * 0.15;
        }

        // Calcular receita líquida
        double receitaLiquida = receitaBruta - despesasInfraestrutura - show.getCache();

        // Determinar status financeiro com base na receita líquida
        StatusFinanceiro statusFinanceiro;
        if (receitaLiquida > 0) {
            statusFinanceiro = StatusFinanceiro.LUCRO;
        } else if (receitaLiquida == 0) {
            statusFinanceiro = StatusFinanceiro.ESTAVEL;
        } else {
            statusFinanceiro = StatusFinanceiro.PREJUIZO;
        }

        // Criar e preencher o relatório
        RelatorioShow relatorio = new RelatorioShow();
        relatorio.setIngressos_normal_vendidos(ingressosNormais);
        relatorio.setIngressos_vip_vendidos(ingressosVIP);
        relatorio.setIngressos_meia_vendidos(ingressosMeiaEntrada);
        relatorio.setReceita_liquida(receitaLiquida);
        relatorio.setStatus_financeiro(statusFinanceiro);

        return relatorio;
    }
}
