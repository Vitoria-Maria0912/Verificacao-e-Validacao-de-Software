package com.vandv.sistema_ingressos.test_show.junit5Tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vandv.sistema_ingressos.model.*;
import com.vandv.sistema_ingressos.repository.ArtistaRepository;
import com.vandv.sistema_ingressos.repository.IngressoRepository;
import com.vandv.sistema_ingressos.repository.LoteRepository;
import com.vandv.sistema_ingressos.repository.ShowRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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

    @ParameterizedTest
    @CsvSource({"0.2,VIP,200.0,160.0", "0.2,MEIA_ENTRADA,200.0,200.0", "0.0,VIP,200.0,200.0", "0.0,MEIA_ENTRADA,200.0,200.0", "0.0,NORMAL,200.0,200.0", "0.5,NORMAL,200.0,100.0"})
    @DisplayName("Teste parametrizado para regras de decisão")
    void testarRegrasDeDecisao(double desconto, TipoIngresso tipo, double precoOriginal, double precoEsperado) throws Exception {
        executarTeste(desconto, tipo, precoOriginal, precoEsperado);
    }

    private void executarTeste(double desconto, TipoIngresso tipo, double precoOriginal, double precoEsperado) throws Exception {
        Artista artista = artistaRepository.save(Artista.builder()
                .nome("Lucas")
                .sobrenome("Pereira")
                .nomeArtistico("Banda LuFi")
                .genero("sertanejo")
                .cpf("870.756.333-90")
                .build());

        Show show = showRepository.save(Show.builder()
                .dataShow(new Date())
                .artista(artista)
                .cache(5000)
                .totalDespesas(10000)
                .lote(new ArrayList<>())
                .dataEspecial(false)
                .build());

        Lote loteX = loteRepository.save(Lote.builder()
                .ingressos(new ArrayList<>())
                .desconto(desconto)
                .qtdIngressos(100)
                .build());

        Ingresso ingressoX = ingressoRepository.save(Ingresso.builder()
                .preco(precoOriginal)
                .tipo(tipo)
                .status(StatusIngresso.DISPONIVEL)
                .build());

        driver.perform(MockMvcRequestBuilders.patch(URI_INGRESSO + "/addIngresso/" + loteX.getId() + "/" + ingressoX.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertEquals(precoEsperado, loteRepository.findById(loteX.getId()).get().getIngressos().get(0).getPreco());
    }

    @Test
    @DirtiesContext
    @Order(1)
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
    @Order(2)
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
    @Order(3)
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
    @Order(4)
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
    @Order(5)
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
    @Order(6)
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
    @Order(7)
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
    @Order(8)
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