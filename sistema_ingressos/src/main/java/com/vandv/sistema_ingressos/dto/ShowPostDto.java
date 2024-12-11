package com.vandv.sistema_ingressos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vandv.sistema_ingressos.model.Artista;
import com.vandv.sistema_ingressos.model.Lote;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShowPostDto {
    @JsonProperty("datashow")
    @NotBlank(message = "Data do show deve ser obrigatório")
    private Date dataShow;

    @JsonProperty("cache")
    @NotBlank(message = "Cache deve ser obrigatório")
    private Integer cache;

    @JsonProperty("totalDespesas")
    @NotBlank(message = "Total das despesas deve ser obrigatório")
    private Integer totalDespesas;

    @JsonProperty("dataEspecial")
    @NotBlank(message = "Data Especial deve ser obrigatório")
    private boolean dataEspecial;

    @JsonProperty("artista")
    @NotBlank(message = "Artista deve ser obrigatório")
    private Artista artista;

    @JsonProperty("lote")
    private List<Lote> lote;
}
