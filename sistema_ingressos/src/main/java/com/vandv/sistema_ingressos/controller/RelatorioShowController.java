package com.vandv.sistema_ingressos.controller;

import com.vandv.sistema_ingressos.service.relatorio.RelatorioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(value = "/v1/relatorio", produces = MediaType.APPLICATION_JSON_VALUE)
public class RelatorioShowController {
    @Autowired
    private RelatorioService relatorioService;


    @GetMapping("/{id}")
    public ResponseEntity<?> relatorioShow(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(relatorioService.geraRelatorio(id));
    }

}