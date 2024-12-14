package com.vandv.sistema_ingressos.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_artista")
public class Artista {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_artista")
    private Long id;

    @JsonProperty("nome")
    @Column(name = "ds_nome", nullable = false)
    private String nome;

    @JsonProperty("sobrenome")
    @Column(name = "ds_sobrenome", nullable = false)
    private String sobrenome;

    @JsonProperty("nomeArtistico")
    @Column(name = "ds_nome_artistico", nullable = false)
    private String nomeArtistico;

    @JsonProperty("genero")
    @Column(name = "ds_genero", nullable = false)
    private String genero;

    @JsonProperty("cpf")
    @Column(name = "ds_cpf", nullable = false)
    private String cpf;
}
