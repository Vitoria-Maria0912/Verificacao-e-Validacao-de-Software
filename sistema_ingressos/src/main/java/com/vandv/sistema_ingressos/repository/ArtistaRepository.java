package com.vandv.sistema_ingressos.repository;

import com.vandv.sistema_ingressos.model.Artista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArtistaRepository extends JpaRepository<Artista, Long> {
}
