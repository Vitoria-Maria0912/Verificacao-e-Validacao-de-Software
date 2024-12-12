package com.vandv.sistema_ingressos.test_show;

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

@AutoConfigureMockMvc
@Transactional
@SpringBootTest
@DisplayName("Testes em relação as funcionalidades de um show")
public class ShowLoteV1ControllerTests {
    final String URI_SHOW = "/v1/show";
    final String URI_INGRESSO = "/v1/ingresso";

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
    Show show_one;
    Artista artista;
    Lote lote;
    Lote lote_one;
    Ingresso ingresso;
    Ingresso ingresso_one;
    Ingresso ingresso_two;
    ShowPostDto showPostDto;

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

        ingresso_one = ingressoRepository.save(Ingresso.builder()
                .preco(10.0)
                .tipo(TipoIngresso.VIP)
                .status(StatusIngresso.DISPONIVEL)
                .build());

        lote = Lote.builder()
                .ingressos(new ArrayList<>())
                .desconto(0.25)
                .build();
        lote.getIngressos().add(ingresso);
        loteRepository.save(lote);

        lote_one = loteRepository.save(Lote.builder()
                .ingressos(new ArrayList<>())
                .desconto(0.25)
                .build());


        showPostDto = ShowPostDto.builder()
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(100000)
                .totalDespesas(30000)
                .lote(new ArrayList<>())
                .dataEspecial(true)
                .build();

        show = showRepository.save(Show.builder()
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(100000)
                .totalDespesas(30000)
                .lote(new ArrayList<>())
                .dataEspecial(true)
                .build());

        show_one = showRepository.save(Show.builder()
                .dataShow(new Date("12/08/2023"))
                .artista(artista)
                .cache(500000)
                .totalDespesas(50000)
                .lote(new ArrayList<>())
                .dataEspecial(false)
                .build());
    }

    @AfterEach
    void tearDown() {
        showRepository.deleteAll();
        artistaRepository.deleteAll();
        loteRepository.deleteAll();
        ingressoRepository.deleteAll();
    }

    @Test
    @DisplayName("Quando adiciono um lote ao show")
    void testQuandoAdicionoLoteAoShow() throws Exception {
        //Act
        String responseJSONString = driver.perform(patch(URI_SHOW + "/addLote/" + show.getId() + "/" + lote.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        //Assert
        assertEquals(2, showRepository.findAll().size());
    }

    @Test
    @DisplayName("Quando adiciono um ingresso ao lote")
    void test_add_ingresso() throws Exception {
        //Act
        String responseJSONString = driver.perform(patch(URI_INGRESSO + "/addIngresso/" + lote_one.getId() + "/" + ingresso_one.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        //Assert
        assertEquals(1, loteRepository.findById(lote_one.getId()).get().getIngressos().size());
        assertEquals(15.0, loteRepository.findById(lote_one.getId()).get().getIngressos().get(0).getPreco());
    }

    @Test
    @DisplayName("Quando modifico a disponibilidade de um ingresso")
    void test_disponibilidade_ingresso() throws Exception {
        test_add_ingresso();
        //Act
        String responseJSONString = driver.perform(patch(URI_INGRESSO + "/modifica-disponibilidade/" + lote_one.getId() + "/" + ingresso_one.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        //Assert
        assertEquals(StatusIngresso.VENDIDO, loteRepository.findById(lote_one.getId()).get().getIngressos().get(0).getStatus());
    }

    @Test
    @DisplayName("Quando adiciono ingresso do tipo NORMAL")
    void test_tipo_Ingresso_Normal() throws Exception {
        //Act
        String responseJSONString = driver.perform(patch(URI_INGRESSO + "/addIngresso/" + ingresso.getId() + "/" +lote.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        //Assert
        assertEquals(TipoIngresso.NORMAL, loteRepository.findById(lote.getId()).get().getIngressos().get(0).getTipo());
    }

    @Test
    @DisplayName("Quando adiciono ingresso do tipo VIP")
    void test_tipo_Ingresso_Vip() throws Exception {
        lote_one.getIngressos().add(ingresso_one);
        loteRepository.save(lote_one);
        //Act
        String responseJSONString = driver.perform(patch(URI_INGRESSO + "/addIngresso/" + ingresso.getId() + "/" +lote_one.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        //Assert
        assertEquals(TipoIngresso.VIP, loteRepository.findById(lote_one.getId()).get().getIngressos().get(0).getTipo());
    }

    @Test
    @DisplayName("Quando adiciono ingresso do tipo Meia Entrada")
    void test_tipo_Ingresso_MeiaEntrada() throws Exception {
        lote.getIngressos().get(0).setTipo(TipoIngresso.MEIA_ENTRADA);
        loteRepository.save(lote);

        assertEquals(TipoIngresso.MEIA_ENTRADA, loteRepository.findById(lote.getId()).get().getIngressos().get(0).getTipo());
    }
}
