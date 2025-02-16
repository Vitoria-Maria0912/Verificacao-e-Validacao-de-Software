package com.vandv.sistema_ingressos.test_show.functionalTests;

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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.ArrayList;
import java.util.Date;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional
@DisplayName("Tabela de Decisões")
public class TabelaDecisao {
    final String URI_RELATORIO = "/v1/relatorio";
    final String URI_INGRESSO = "/v1/ingresso";
    final String URI_SHOW = "/v1/show";

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
        // Limpar tabelas específicas antes de cada teste
        ingressoRepository.deleteAll();
        loteRepository.deleteAll();
        showRepository.deleteAll();
        artistaRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        showRepository.deleteAll();
        artistaRepository.deleteAll();
        loteRepository.deleteAll();
        ingressoRepository.deleteAll();
    }

    @Test
    @DirtiesContext
    @DisplayName("Regra One")
    void RegraOne() throws Exception {
        // Criar artista
        Artista artista = artistaRepository.save(Artista.builder()
                .nome("Lucas")
                .sobrenome("Pereira")
                .nomeArtistico("Banda LuFi")
                .genero("sertanejo")
                .cpf("870.756.333-90")
                .build());

        // Criar show
        Show show = showRepository.save(Show.builder()
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(5000)
                .totalDespesas(10000)
                .lote(new ArrayList<>())
                .dataEspecial(false)
                .build());

        Lote loteX = loteRepository.save(Lote.builder()
                .ingressos(new ArrayList<>())
                .desconto(0.2)
                .qtdIngressos(100)
                .build());

        Ingresso ingressoX = ingressoRepository.save(Ingresso.builder()
                .preco(200.0)
                .tipo(TipoIngresso.VIP)
                .status(StatusIngresso.DISPONIVEL)
                .build());

        //Act
        String responseJSONString = driver.perform(patch(URI_INGRESSO + "/addIngresso/" + loteX.getId() + "/" + ingressoX.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        //Assert
        assertEquals(160.0, loteRepository.findById(1L).get().getIngressos().get(0).getPreco());
    }

    @Test
    @DirtiesContext
    @DisplayName("Regra Two")
    void RegraTwo() throws Exception {
        // Criar artista
        Artista artista = artistaRepository.save(Artista.builder()
                .nome("Lucas")
                .sobrenome("Pereira")
                .nomeArtistico("Banda LuFi")
                .genero("sertanejo")
                .cpf("870.756.333-90")
                .build());

        // Criar show
        Show show = showRepository.save(Show.builder()
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(5000)
                .totalDespesas(10000)
                .lote(new ArrayList<>())
                .dataEspecial(false)
                .build());

        Lote loteX = loteRepository.save(Lote.builder()
                .ingressos(new ArrayList<>())
                .desconto(0.2)
                .qtdIngressos(100)
                .build());

        Ingresso ingressoX = ingressoRepository.save(Ingresso.builder()
                .preco(200.0)
                .tipo(TipoIngresso.MEIA_ENTRADA)
                .status(StatusIngresso.DISPONIVEL)
                .build());

        //Act
        String responseJSONString = driver.perform(patch(URI_INGRESSO + "/addIngresso/" + loteX.getId() + "/" + ingressoX.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        //Assert
        assertEquals(200.00, loteRepository.findById(1L).get().getIngressos().get(0).getPreco());
    }

    @Test
    @DirtiesContext
    @DisplayName("Regra Three")
    void RegraThree() throws Exception {
        // Criar artista
        Artista artista = artistaRepository.save(Artista.builder()
                .nome("Lucas")
                .sobrenome("Pereira")
                .nomeArtistico("Banda LuFi")
                .genero("sertanejo")
                .cpf("870.756.333-90")
                .build());

        // Criar show
        Show show = showRepository.save(Show.builder()
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(5000)
                .totalDespesas(10000)
                .lote(new ArrayList<>())
                .dataEspecial(false)
                .build());

        Lote loteX = loteRepository.save(Lote.builder()
                .ingressos(new ArrayList<>())
                .desconto(0.0)
                .qtdIngressos(100)
                .build());

        Ingresso ingressoX = ingressoRepository.save(Ingresso.builder()
                .preco(200.0)
                .tipo(TipoIngresso.VIP)
                .status(StatusIngresso.DISPONIVEL)
                .build());

        //Act
        String responseJSONString = driver.perform(patch(URI_INGRESSO + "/addIngresso/" + loteX.getId() + "/" + ingressoX.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        //Assert
        assertEquals(200.00, loteRepository.findById(1L).get().getIngressos().get(0).getPreco());
    }

    @Test
    @DirtiesContext
    @DisplayName("Regra Four")
    void RegraFour() throws Exception {
        // Criar artista
        Artista artista = artistaRepository.save(Artista.builder()
                .nome("Lucas")
                .sobrenome("Pereira")
                .nomeArtistico("Banda LuFi")
                .genero("sertanejo")
                .cpf("870.756.333-90")
                .build());

        // Criar show
        Show show = showRepository.save(Show.builder()
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(5000)
                .totalDespesas(10000)
                .lote(new ArrayList<>())
                .dataEspecial(false)
                .build());

        Lote loteX = loteRepository.save(Lote.builder()
                .ingressos(new ArrayList<>())
                .desconto(0.0)
                .qtdIngressos(100)
                .build());

        Ingresso ingressoX = ingressoRepository.save(Ingresso.builder()
                .preco(200.0)
                .tipo(TipoIngresso.MEIA_ENTRADA)
                .status(StatusIngresso.DISPONIVEL)
                .build());

        //Act
        String responseJSONString = driver.perform(patch(URI_INGRESSO + "/addIngresso/" + loteX.getId() + "/" + ingressoX.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        //Assert
        assertEquals(200.00, loteRepository.findById(1L).get().getIngressos().get(0).getPreco());
    }

    @Test
    @DirtiesContext
    @DisplayName("Regra Five")
    void RegraFive() throws Exception {
        // Criar artista
        Artista artista = artistaRepository.save(Artista.builder()
                .nome("Lucas")
                .sobrenome("Pereira")
                .nomeArtistico("Banda LuFi")
                .genero("sertanejo")
                .cpf("870.756.333-90")
                .build());

        // Criar show
        Show show = showRepository.save(Show.builder()
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(5000)
                .totalDespesas(10000)
                .lote(new ArrayList<>())
                .dataEspecial(false)
                .build());

        Lote loteX = loteRepository.save(Lote.builder()
                .ingressos(new ArrayList<>())
                .desconto(0.0)
                .qtdIngressos(100)
                .build());

        Ingresso ingressoX = ingressoRepository.save(Ingresso.builder()
                .preco(200.0)
                .tipo(TipoIngresso.NORMAL)
                .status(StatusIngresso.DISPONIVEL)
                .build());

        //Act
        String responseJSONString = driver.perform(patch(URI_INGRESSO + "/addIngresso/" + loteX.getId() + "/" + ingressoX.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        //Assert
        assertEquals(200.00, loteRepository.findById(1L).get().getIngressos().get(0).getPreco());
    }

    @Test
    @DisplayName("Regra Six")
    @DirtiesContext
    void RegraSix() throws Exception {
        // Criar artista
        Artista artista = artistaRepository.save(Artista.builder()
                .nome("Lucas")
                .sobrenome("Pereira")
                .nomeArtistico("Banda LuFi")
                .genero("sertanejo")
                .cpf("870.756.333-90")
                .build());

        // Criar show
        Show show = showRepository.save(Show.builder()
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(5000)
                .totalDespesas(10000)
                .lote(new ArrayList<>())
                .dataEspecial(false)
                .build());

        Lote loteX = loteRepository.save(Lote.builder()
                .ingressos(new ArrayList<>())
                .desconto(0.5)
                .qtdIngressos(100)
                .build());

        Ingresso ingressoX = ingressoRepository.save(Ingresso.builder()
                .preco(200.0)
                .tipo(TipoIngresso.NORMAL)
                .status(StatusIngresso.DISPONIVEL)
                .build());

        //Act
        String responseJSONString = driver.perform(patch(URI_INGRESSO + "/addIngresso/" + loteX.getId() + "/" + ingressoX.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        //Assert
        assertEquals(100.00, loteRepository.findById(1L).get().getIngressos().get(0).getPreco());
    }

    @Test
    @DisplayName("Regra One")
    @DirtiesContext
    void ShowComDataEspecial() throws Exception {
        Artista artista = artistaRepository.save(Artista.builder()
                .id(1L)
                .nome("Lucas")
                .sobrenome("Pereira")
                .nomeArtistico("Banda LuFi")
                .genero("sertanejo")
                .cpf("870.756.333-90")
                .build());
        Show show = Show.builder()
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(100000)
                .totalDespesas(30000)
                .lote(new ArrayList<>())
                .dataEspecial(false)
                .build();

        showRepository.save(show);
        //Act
        String responseJSONString = driver.perform(get(URI_RELATORIO + "/" + show.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        RelatorioShow relatorioShow1 = objectMapper.readValue(responseJSONString, RelatorioShow.class);

        //Assert
        assertEquals(StatusFinanceiro.PREJUIZO, relatorioShow1.getStatus_financeiro());
    }

    @Test
    @DirtiesContext
    @DisplayName("Regra Two")
    void ShowNaoPossuiDataEspecial() throws Exception {
        Artista artista = artistaRepository.save(Artista.builder()
                .id(1L)
                .nome("Lucas")
                .sobrenome("Pereira")
                .nomeArtistico("Banda LuFi")
                .genero("sertanejo")
                .cpf("870.756.333-90")
                .build());
        Show show1 = Show.builder()
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(100000)
                .totalDespesas(30000)
                .lote(new ArrayList<>())
                .dataEspecial(false)
                .build();

        showRepository.save(show1);
        //Act
        String responseJSONString = driver.perform(get(URI_RELATORIO + "/" + show1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        RelatorioShow relatorioShow1 = objectMapper.readValue(responseJSONString, RelatorioShow.class);

        //Assert
        assertEquals(StatusFinanceiro.PREJUIZO, relatorioShow1.getStatus_financeiro());
    }
}