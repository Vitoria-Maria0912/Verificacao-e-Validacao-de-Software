package com.vandv.sistema_ingressos.repository;

import com.vandv.sistema_ingressos.model.Lote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoteRepository extends JpaRepository<Lote, Long> {
}
