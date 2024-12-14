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
        ingressoRepository.deleteAll();
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
        assertEquals(0, relatorioShow1.getIngressos_normal_vendidos());
    }

    @Test
    @DirtiesContext
    @DisplayName("Gerando o relatório do show com 1 lote e 1 ingresso vendido")
    void testIngressoVendido() throws Exception {
        show.getLote().get(0).getIngressos().get(0).setStatus(StatusIngresso.VENDIDO);
        //Act
        String responseJSONString = driver.perform(get(URI_RELATORIO + "/" + show.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        RelatorioShow relatorioShow = objectMapper.readValue(responseJSONString, RelatorioShow.class);

        //Assert
        assertEquals(1, relatorioShow.getIngressos_normal_vendidos());
    }

    @Test
    @DirtiesContext
    @DisplayName("Gerando o relatório do show com 2 lote e + ingressos vendidos")
    void testComMaisDeUmLote() throws Exception {
        Ingresso ingresso1 = ingressoRepository.save(Ingresso.builder()
                .preco(40.0)
                .tipo(TipoIngresso.NORMAL)
                .status(StatusIngresso.VENDIDO)
                .build());

        Ingresso ingresso2 = ingressoRepository.save(Ingresso.builder()
                .preco(40.0)
                .tipo(TipoIngresso.VIP)
                .status(StatusIngresso.VENDIDO)
                .build());

        Lote lote1 = Lote.builder()
                .ingressos(new ArrayList<>())
                .qtdIngressos(400)
                .desconto(0.25)
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
        assertEquals(1, relatorioShow.getIngressos_normal_vendidos());
        assertEquals(1, relatorioShow.getIngressos_vip_vendidos());
    }

    @Test
    @DirtiesContext
    @DisplayName("Gerando o relatório do show com 2 lote e + ingressos vendidos")
    void testComMa() throws Exception {
        Ingresso ingresso1 = ingressoRepository.save(Ingresso.builder()
                .preco(40.0)
                .tipo(TipoIngresso.NORMAL)
                .status(StatusIngresso.VENDIDO)
                .build());

        Ingresso ingresso2 = ingressoRepository.save(Ingresso.builder()
                .preco(40.0)
                .tipo(TipoIngresso.VIP)
                .status(StatusIngresso.VENDIDO)
                .build());

        Lote lote1 = Lote.builder()
                .ingressos(new ArrayList<>())
                .desconto(0.25)
                .qtdIngressos(400)
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
        assertEquals(-134420.0, relatorioShow.getReceita_liquida());
    }

}

