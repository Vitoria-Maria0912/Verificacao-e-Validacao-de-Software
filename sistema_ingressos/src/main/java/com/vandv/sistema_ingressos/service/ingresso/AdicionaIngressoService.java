package com.vandv.sistema_ingressos.service.ingresso;


public interface AdicionaIngressoService {
    void adicionarIngresso(Long idLote, Long idIngresso);
    void modificaDisponibilidade(Long idLote, Long idIngresso);
}
