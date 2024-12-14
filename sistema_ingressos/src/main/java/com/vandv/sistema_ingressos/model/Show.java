package com.vandv.sistema_ingressos.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_show")
public class Show {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_show")
    private Long id;

    @JsonProperty("dataShow")
    @Column(name="ds_data", nullable = false)
    private Date dataShow;

    @JsonProperty("cache")
    @Column(name="ds_cache", nullable = false)
    private Integer cache;

    @JsonProperty("totalDespesas")
    @Column(name = "ds_totalDespesas", nullable = false)
    private Integer totalDespesas;

    @JsonProperty("dataEspecial")
    @Column(name = "ds_dataEspecial", nullable = false)
    private boolean dataEspecial;

    @JsonProperty("artista")
    @JoinColumn(name = "ds_artista", nullable = true)
    @OneToOne(fetch = FetchType.EAGER)
    private Artista artista;

    @JsonProperty("lote")
    @Column(name = "ds_lotes_ingresso", nullable = false)
    @OneToMany(fetch = FetchType.EAGER)
    private List<Lote> lote;
}
