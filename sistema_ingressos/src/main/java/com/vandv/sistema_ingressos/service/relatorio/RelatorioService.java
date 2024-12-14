package com.vandv.sistema_ingressos.service.relatorio;

import com.vandv.sistema_ingressos.model.RelatorioShow;

@FunctionalInterface
public interface RelatorioService {
    RelatorioShow geraRelatorio(Long idShow);
}
