package com.vandv.sistema_ingressos.service.ingresso;

import com.vandv.sistema_ingressos.model.Ingresso;
import com.vandv.sistema_ingressos.model.Lote;
import com.vandv.sistema_ingressos.model.StatusIngresso;
import com.vandv.sistema_ingressos.model.TipoIngresso;
import com.vandv.sistema_ingressos.repository.IngressoRepository;
import com.vandv.sistema_ingressos.repository.LoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdicionaIngressoPadraoService implements AdicionaIngressoService{
    @Autowired
    LoteRepository loteRepository;
    @Autowired
    IngressoRepository ingressoRepository;
    @Override
    public void adicionarIngresso(Long idLote, Long idIngresso) {
        Lote lote = loteRepository.findById(idLote).get();
        Ingresso ingresso = ingressoRepository.findById(idIngresso).get();

        int qtdTotalIngressos = lote.getQtdIngressos();

        // Contagem de ingressos já existentes no lote, por tipo
        int qtdVIP = contarIngressosPorTipo(lote, TipoIngresso.VIP);
        int qtdMeiaEntrada = contarIngressosPorTipo(lote, TipoIngresso.MEIA_ENTRADA);
        int qtdNormal = contarIngressosPorTipo(lote, TipoIngresso.NORMAL);

        // Defina os limites para cada tipo de ingresso
        int qtdIngressosVIPMax = calcularLimiteIngressos(qtdTotalIngressos, 0.30);
        int qtdIngressosMeiaMax = calcularLimiteIngressos(qtdTotalIngressos, 0.10);
        int qtdIngressosNormalMax = calcularLimiteIngressos(qtdTotalIngressos, 1.0) - qtdIngressosVIPMax - qtdIngressosMeiaMax;

        // Verifique os limites para cada tipo de ingresso
        verificarLimiteIngressos(ingresso, qtdVIP, qtdIngressosVIPMax, TipoIngresso.VIP, "Limite de ingressos VIP atingido.");
        verificarLimiteIngressos(ingresso, qtdMeiaEntrada, qtdIngressosMeiaMax, TipoIngresso.MEIA_ENTRADA, "Limite de ingressos Meia Entrada atingido.");
        verificarLimiteIngressos(ingresso, qtdNormal, qtdIngressosNormalMax, TipoIngresso.NORMAL, "Limite de ingressos NORMAL atingido.");
        // Ajuste o preço do ingresso com base nos descontos e no tipo
        double precoCalculado = calcularPrecoIngresso(ingresso, lote);

        // Atualiza o ingresso com o novo preço e o salva
        ingresso.setPreco(precoCalculado);
        ingressoRepository.save(ingresso);

        // Adiciona o ingresso ao lote e salva
        lote.getIngressos().add(ingresso);
        loteRepository.save(lote);
    }

    private int contarIngressosPorTipo(Lote lote, TipoIngresso tipo) {
        return (int) lote.getIngressos().stream()
                .filter(i -> i.getTipo().equals(tipo))
                .count();
    }

    private int calcularLimiteIngressos(int qtdTotalIngressos, double percentual) {
        return (int) (qtdTotalIngressos * percentual);
    }

    private void verificarLimiteIngressos(Ingresso ingresso, int qtdAtual, int qtdMax, TipoIngresso tipo, String erroMsg) {
        if (ingresso.getTipo().equals(tipo) && qtdAtual >= qtdMax) {
            throw new IllegalStateException(erroMsg);
        }
    }

    private double calcularPrecoIngresso(Ingresso ingresso, Lote lote) {
        double preco = ingresso.getPreco();
        if (ingresso.getTipo().equals(TipoIngresso.NORMAL) || ingresso.getTipo().equals(TipoIngresso.VIP)) {
            preco -= (ingresso.getPreco() * lote.getDesconto());
        }
        return preco;
    }


    @Override
    public void modificaDisponibilidade(Long idLote, Long idIngresso) {
        Lote lote = loteRepository.findById(idLote).get();
        Ingresso ingresso = ingressoRepository.findById(idIngresso).get();
        ingresso.setStatus(StatusIngresso.VENDIDO);
        ingressoRepository.save(ingresso);
        loteRepository.save(lote);
    }
}
