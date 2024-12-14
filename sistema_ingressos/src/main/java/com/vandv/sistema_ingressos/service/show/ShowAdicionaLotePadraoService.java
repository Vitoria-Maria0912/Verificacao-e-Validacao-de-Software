package com.vandv.sistema_ingressos.service.show;

import com.vandv.sistema_ingressos.model.Lote;
import com.vandv.sistema_ingressos.model.Show;
import com.vandv.sistema_ingressos.repository.LoteRepository;
import com.vandv.sistema_ingressos.repository.ShowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShowAdicionaLotePadraoService implements ShowAdicionaLoteService {
    @Autowired
    ShowRepository showRepository;
    @Autowired
    LoteRepository loteRepository;

    @Override
    public void adicionaLote(Long idLote, Long idShow) {
        Lote lote = loteRepository.findById(idLote).get();
        Show show = showRepository.findById(idShow).get();
        show.getLote().add(lote);
        showRepository.save(show);
    }
}
