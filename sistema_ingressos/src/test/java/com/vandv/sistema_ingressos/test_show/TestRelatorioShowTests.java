package com.vandv.sistema_ingressos.test_show;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vandv.sistema_ingressos.dto.ShowPostDto;
import com.vandv.sistema_ingressos.model.*;
import com.vandv.sistema_ingressos.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@AutoConfigureMockMvc
@Transactional
@SpringBootTest
@DisplayName("Testes em relação ao relatório de um show")
public class TestRelatorioShowTests {
    final String URI_RELATORIO = "/v1/relatorio";

    // Repositórios
    @Autowired
    ShowRepository showRepository;
    @Autowired
    ArtistaRepository artistaRepository;
    @Autowired
    LoteRepository loteRepository;
    @Autowired
    IngressoRepository ingressoRepository;

    // Objetos de mapeamento e dados
    ObjectMapper objectMapper = new ObjectMapper();
    ModelMapper modelMapper = new ModelMapper();

    // Instâncias de entidades
    Show show;
    Artista artista;
    Lote lote;
    ShowPostDto showPostDto;
    Ingresso ingresso;
    RelatorioShow relatorioShow;
    // MockMvc configurado manualmente
    private MockMvc driver;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setup() {
        // Registrar módulo para data de tipo Java 8
        objectMapper.registerModule(new JavaTimeModule());

        // Configuração do MockMvc
        driver = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Dados para os testes
        artista = artistaRepository.save(Artista.builder()
                .id(1L)
                .nome("Lucas")
                .sobrenome("Pereira")
                .nomeArtistico("Banda LuFi")
                .genero("sertanejo")
                .cpf("870.756.333-90")
                .build());
        ingresso = ingressoRepository.save(Ingresso.builder()
                .preco(40.0)
                .tipo(TipoIngresso.NORMAL)
                .status(StatusIngresso.DISPONIVEL)
                .build());

        lote = Lote.builder()
                .ingressos(new ArrayList<>())
                .desconto(0.25)
                .build();
        lote.getIngressos().add(ingresso);
        loteRepository.save(lote);

        showPostDto = ShowPostDto.builder()
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(100000)
                .totalDespesas(30000)
                .lote(new ArrayList<>())
                .dataEspecial(true)
                .build();

        show = Show.builder()
                .id(1L)
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(100000)
                .totalDespesas(30000)
                .lote(new ArrayList<>())
                .dataEspecial(true)
                .build();
        show.getLote().add(lote);
        showRepository.save(show);
    }

    @AfterEach
    void tearDown() {
        showRepository.deleteAll();
        artistaRepository.deleteAll();
        loteRepository.deleteAll();
    }

    @Test
    @DisplayName("Gerando o relatório do show com 1 lote")
    void testQuandoBuscoUmShow() throws Exception {
        //Act
        String responseJSONString = driver.perform(get(URI_RELATORIO + "/" + show.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        RelatorioShow relatorioShow1 = objectMapper.readValue(responseJSONString, RelatorioShow.class);

        //Assert
        assertEquals(1, relatorioShow1.getIngressos_normal_vendidos());
    }
}

