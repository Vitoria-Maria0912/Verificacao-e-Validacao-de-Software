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

        // Número total de ingressos permitido para o lote
        int qtdTotalIngressos = lote.getQtdIngressos();

        // Contagem de ingressos já existentes no lote, por tipo
        int qtdVIP = (int) lote.getIngressos().stream().filter(i -> i.getTipo().equals(TipoIngresso.VIP)).count();
        int qtdMeiaEntrada = (int) lote.getIngressos().stream().filter(i -> i.getTipo().equals(TipoIngresso.MEIA_ENTRADA)).count();
        int qtdNormal = (int) lote.getIngressos().stream().filter(i -> i.getTipo().equals(TipoIngresso.NORMAL)).count();

        // Defina os limites para cada tipo de ingresso
        int qtdIngressosVIPMax = (int) (qtdTotalIngressos * 0.30); // 30% do total
        int qtdIngressosMeiaMax = (int) (qtdTotalIngressos * 0.10); // 10% do total
        int qtdIngressosNormalMax = qtdTotalIngressos - qtdIngressosVIPMax - qtdIngressosMeiaMax; // O restante é para NORMAL

        // Verifique se o ingresso é do tipo VIP e se ainda há espaço para mais ingressos VIP
        if (ingresso.getTipo().equals(TipoIngresso.VIP) && qtdVIP >= qtdIngressosVIPMax) {
            throw new IllegalStateException("Limite de ingressos VIP atingido.");
        }

        // Verifique se o ingresso é do tipo MEIA_ENTRADA e se ainda há espaço para mais ingressos MEIA_ENTRADA
        if (ingresso.getTipo().equals(TipoIngresso.MEIA_ENTRADA) && qtdMeiaEntrada >= qtdIngressosMeiaMax) {
            throw new IllegalStateException("Limite de ingressos Meia Entrada atingido.");
        }

        // Verifique se o ingresso é do tipo NORMAL e se ainda há espaço para mais ingressos NORMAL
        if (ingresso.getTipo().equals(TipoIngresso.NORMAL) && qtdNormal >= qtdIngressosNormalMax) {
            throw new IllegalStateException("Limite de ingressos NORMAL atingido.");
        }

        // Ajuste o preço do ingresso com base nos descontos e no tipo
        double preco = ingresso.getPreco();
        if (ingresso.getTipo().equals(TipoIngresso.NORMAL) ||
                ingresso.getTipo().equals(TipoIngresso.VIP)) {
            preco -= (ingresso.getPreco() * lote.getDesconto());
        }
        double precoCalculado = ingresso.getTipo().calcularPreco(preco);
        ingresso.setPreco(precoCalculado);

        // Salve o ingresso no repositório e adicione ao lote
        ingressoRepository.save(ingresso);
        lote.getIngressos().add(ingresso);
        loteRepository.save(lote);
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
