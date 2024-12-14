package com.vandv.sistema_ingressos.repository;

import com.vandv.sistema_ingressos.model.Show;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShowRepository extends JpaRepository<Show, Long> {
}
