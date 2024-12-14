package com.vandv.sistema_ingressos.controller;

import com.vandv.sistema_ingressos.dto.ShowPostDto;
import com.vandv.sistema_ingressos.model.Show;
import com.vandv.sistema_ingressos.service.show.ShowAdicionaLoteService;
import com.vandv.sistema_ingressos.service.show.ShowCrudService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/v1/show", produces = MediaType.APPLICATION_JSON_VALUE)
public class ShowController {
    @Autowired
    private ShowCrudService showCrudService;
    @Autowired
    ShowAdicionaLoteService showAdicionaLoteService;

    @PostMapping
    public ResponseEntity<?> createProduto(
            @RequestBody @Valid ShowPostDto showPostDto
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(showCrudService.showCreate(showPostDto));
    }

    @GetMapping
    public ResponseEntity<List<Show>> findAll() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(showCrudService.showFindAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(showCrudService.showFindById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduto(@PathVariable Long id) {
        this.showCrudService.showDelete(id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body("");
    }

    @PatchMapping("/addLote/{idLote}/{idShow}")
    public ResponseEntity<?> clienteInteressaPromocao(
            @PathVariable Long idLote,
            @PathVariable Long idShow
    ) {
        this.showAdicionaLoteService.adicionaLote(idLote, idShow);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body("");
    }
}