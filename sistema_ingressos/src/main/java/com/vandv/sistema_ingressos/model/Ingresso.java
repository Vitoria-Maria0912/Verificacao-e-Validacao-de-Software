package com.vandv.sistema_ingressos.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_ingresso")
public class Ingresso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ingresso")
    private Long id;

    @JsonProperty("tipo")
    @Column(name = "ds_tipo", nullable = false)
    private Enum tipo;

    @JsonProperty("status")
    @Column(name = "ds_status", nullable = false)
    private Enum status;

    @JsonProperty("preco")
    @Column(name = "ds_preco", nullable = false)
    private Double preco;

}
