package com.vandv.sistema_ingressos.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_lote_ingresso")
public class Lote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lote")
    private Long id;

    @JsonProperty("desconto")
    @Column(name = "ds_desconto", nullable = false)
    private Double desconto;

    @JsonProperty("ingressos")
    @Column(name = "ds_ingressos", nullable = false)
    @OneToMany()
    private List<Ingresso> ingressos;
}
