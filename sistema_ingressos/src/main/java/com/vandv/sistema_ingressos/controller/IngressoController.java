package com.vandv.sistema_ingressos.controller;

import com.vandv.sistema_ingressos.service.ingresso.AdicionaIngressoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/v1/ingresso", produces = MediaType.APPLICATION_JSON_VALUE)
public class IngressoController {
    @Autowired
    private AdicionaIngressoService adicionaIngressoService;

    @PatchMapping("/addIngresso/{idIngresso}/{idLote}")
    public ResponseEntity<?> adicionaIngresso(
            @PathVariable Long idIngresso,
            @PathVariable Long idLote
    ) {
        this.adicionaIngressoService.adicionarIngresso(idLote, idIngresso);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body("");
    }

    @PatchMapping("/modifica-disponibilidade/{idIngresso}/{idLote}")
    public ResponseEntity<?> modificaDisponibilidadeIngresso(
            @PathVariable Long idIngresso,
            @PathVariable Long idLote
    ) {
        this.adicionaIngressoService.modificaDisponibilidade(idLote, idIngresso);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body("");
    }
}
