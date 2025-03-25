package com.vandv.sistema_ingressos.test_show.junit5Tests;

import com.vandv.sistema_ingressos.dto.ShowPostDto;
import com.vandv.sistema_ingressos.model.*;
import com.vandv.sistema_ingressos.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.ArrayList;
import java.util.Date;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Partição por Equivalência")
public class ParticaoPorEquivalencia {
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

    // Instâncias de entidades
    Show show;
    Artista artista;
    Lote lote;
    ShowPostDto showPostDto;
    Ingresso ingresso;
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
                .qtdIngressos(400)
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
                .cache(100)
                .totalDespesas(67)
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
        ingressoRepository.deleteAll();
    }

    @Test
    @DirtiesContext
    @Order(1)
    @DisplayName("Show com Data Especial")
    void ShowComDataEspecial() throws Exception {
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

    @ParameterizedTest
    @CsvSource({"true,PREJUIZO", "false,PREJUIZO"})
    @DisplayName("Teste parametrizado para diferentes datas especiais")
    void testarDataEspecial(boolean dataEspecial, StatusFinanceiro esperado) throws Exception {
        show.setDataEspecial(dataEspecial);
        showRepository.save(show);

        String responseJSONString = driver.perform(MockMvcRequestBuilders.get(URI_RELATORIO + "/" + show.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        RelatorioShow relatorioShow = objectMapper.readValue(responseJSONString, RelatorioShow.class);
        assertEquals(esperado, relatorioShow.getStatus_financeiro());
    }


    @Test
    @Order(2)
    @DirtiesContext
    @DisplayName("Show não possui Data Especial")
    void ShowNaoPossuiDataEspecial() throws Exception {
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

    @Test
    @Order(3)
    @DirtiesContext
    @DisplayName("Show coom Lucro")
    void ShowComLucro() throws Exception {
        Ingresso ingresso1 = ingressoRepository.save(Ingresso.builder()
                .preco(100.0)
                .tipo(TipoIngresso.NORMAL)
                .status(StatusIngresso.VENDIDO)
                .build());

        Ingresso ingresso2 = ingressoRepository.save(Ingresso.builder()
                .preco(400.0)
                .tipo(TipoIngresso.VIP)
                .status(StatusIngresso.VENDIDO)
                .build());

        Lote lote1 = Lote.builder()
                .ingressos(new ArrayList<>())
                .desconto(0.0)
                .qtdIngressos(2)
                .build();
        lote1.getIngressos().add(ingresso1);
        lote1.getIngressos().add(ingresso2);
        loteRepository.save(lote1);
        show.getLote().add(lote1);
        showRepository.save(show);
        //Act
        String responseJSONString = driver.perform(get(URI_RELATORIO + "/" + show.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        RelatorioShow relatorioShow = objectMapper.readValue(responseJSONString, RelatorioShow.class);

        //Assert
        assertEquals(StatusFinanceiro.LUCRO, relatorioShow.getStatus_financeiro());
    }

    @Test
    @Order(4)
    @DirtiesContext
    @DisplayName("Show Estável")
    void ShowEstavel() throws Exception {
        Ingresso ingresso1 = ingressoRepository.save(Ingresso.builder()
                .preco(169.0)
                .tipo(TipoIngresso.NORMAL)
                .status(StatusIngresso.VENDIDO)
                .build());

        Ingresso ingresso2 = ingressoRepository.save(Ingresso.builder()
                .preco(7.05)
                .tipo(TipoIngresso.VIP)
                .status(StatusIngresso.VENDIDO)
                .build());

        Lote lote1 = Lote.builder()
                .ingressos(new ArrayList<>())
                .desconto(0.0)
                .qtdIngressos(2)
                .build();
        lote1.getIngressos().add(ingresso1);
        lote1.getIngressos().add(ingresso2);
        loteRepository.save(lote1);
        show.getLote().add(lote1);
        showRepository.save(show);
        //Act
        String responseJSONString = driver.perform(get(URI_RELATORIO + "/" + show.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        RelatorioShow relatorioShow = objectMapper.readValue(responseJSONString, RelatorioShow.class);

        //Assert
        assertEquals(StatusFinanceiro.ESTAVEL, relatorioShow.getStatus_financeiro());
    }

    @Test
    @Order(5)
    @DirtiesContext
    @DisplayName("Show Com Prejuízo")
    void ShowComPrejuizo() throws Exception {
        Ingresso ingresso1 = ingressoRepository.save(Ingresso.builder()
                .preco(70.0)
                .tipo(TipoIngresso.NORMAL)
                .status(StatusIngresso.VENDIDO)
                .build());

        Ingresso ingresso2 = ingressoRepository.save(Ingresso.builder()
                .preco(7.05)
                .tipo(TipoIngresso.VIP)
                .status(StatusIngresso.VENDIDO)
                .build());

        Lote lote1 = Lote.builder()
                .ingressos(new ArrayList<>())
                .desconto(0.0)
                .qtdIngressos(2)
                .build();
        lote1.getIngressos().add(ingresso1);
        lote1.getIngressos().add(ingresso2);
        loteRepository.save(lote1);
        show.getLote().add(lote1);
        showRepository.save(show);
        //Act
        String responseJSONString = driver.perform(get(URI_RELATORIO + "/" + show.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        RelatorioShow relatorioShow = objectMapper.readValue(responseJSONString, RelatorioShow.class);

        //Assert
        assertEquals(StatusFinanceiro.PREJUIZO, relatorioShow.getStatus_financeiro());
    }

    @Test
    @Order(6)
    @DisplayName("Ingresso Vendido")
    void IngressoVendido() throws Exception {
        Ingresso ingresso = ingressoRepository.save(Ingresso.builder()
                .preco(10.0)
                .tipo(TipoIngresso.NORMAL)
                .status(StatusIngresso.DISPONIVEL)
                .build());

        lote = Lote.builder()
                .ingressos(new ArrayList<>())
                .desconto(0.15)
                .qtdIngressos(400)
                .build();
        lote.getIngressos().add(ingresso);
        loteRepository.save(lote);

        //Act
        String responseJSONString = driver.perform(patch(URI_INGRESSO + "/modifica-disponibilidade/" + lote.getId() + "/" + ingresso.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        //Assert
        assertEquals(StatusIngresso.VENDIDO, loteRepository.findById(lote.getId()).get().getIngressos().get(0).getStatus());
    }

    @Test
    @Order(7)
    @DisplayName("Ingresso Não Vendido")
    void IngressoNaoVendido() throws Exception {
        Ingresso ingresso = ingressoRepository.save(Ingresso.builder()
                .preco(10.0)
                .tipo(TipoIngresso.NORMAL)
                .status(StatusIngresso.DISPONIVEL)
                .build());

        lote = Lote.builder()
                .ingressos(new ArrayList<>())
                .desconto(0.15)
                .qtdIngressos(400)
                .build();
        lote.getIngressos().add(ingresso);
        loteRepository.save(lote);

        assertEquals(StatusIngresso.DISPONIVEL, loteRepository.findById(lote.getId()).get().getIngressos().get(0).getStatus());
    }

}
