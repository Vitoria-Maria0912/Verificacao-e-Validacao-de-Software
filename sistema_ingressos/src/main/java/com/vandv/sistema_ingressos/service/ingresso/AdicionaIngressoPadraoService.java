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

        double preco = ingresso.getPreco();
        if (ingresso.getTipo().equals(TipoIngresso.NORMAL) ||
                ingresso.getTipo().equals(TipoIngresso.VIP)) {
            preco -= (ingresso.getPreco() * lote.getDesconto());
        }
        double precoCalculado = ingresso.getTipo().calcularPreco(preco);
        ingresso.setPreco(precoCalculado);
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
