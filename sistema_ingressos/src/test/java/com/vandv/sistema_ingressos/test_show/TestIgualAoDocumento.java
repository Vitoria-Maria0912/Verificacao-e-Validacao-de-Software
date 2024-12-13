package com.vandv.sistema_ingressos.test_show;

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
public class TestIgualAoDocumento {
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
    Ingresso ingresso;
    Ingresso ingresso2;
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

        lote = Lote.builder()
                .ingressos(new ArrayList<>())
                .desconto(0.15)
                .qtdIngressos(500)
                .build();
        loteRepository.save(lote);

        show = Show.builder()
                .id(1L)
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(1000)
                .totalDespesas(2000)
                .lote(new ArrayList<>())
                .dataEspecial(true)
                .build();
        // Adiciona o lote ao show
        show.getLote().add(lote);

        // Criação de ingressos para o lote
        for (int i = 0; i < 100; i++) {
            Ingresso ingressoNovo = Ingresso.builder()
                    .preco(17.0)
                    .tipo(TipoIngresso.VIP)
                    .status(StatusIngresso.VENDIDO)
                    .build();

            // Salva o ingresso no banco de dados
            ingressoRepository.save(ingressoNovo);

            // Associa o ingresso ao lote
            lote.getIngressos().add(ingressoNovo);
        }

        // Criação de ingressos para o lote
        for (int i = 0; i < 50; i++) {
            Ingresso ingressoNovo = Ingresso.builder()
                    .preco(5.0)
                    .tipo(TipoIngresso.MEIA_ENTRADA)
                    .status(StatusIngresso.VENDIDO)
                    .build();

            // Salva o ingresso no banco de dados
            ingressoRepository.save(ingressoNovo);

            // Associa o ingresso ao lote
            lote.getIngressos().add(ingressoNovo);
        }

        // Segunda rodada de criação de ingressos
        for (int i = 0; i < 350; i++) {
            Ingresso ingressoNovo = Ingresso.builder()
                    .preco(8.5)
                    .tipo(TipoIngresso.NORMAL)
                    .status(StatusIngresso.VENDIDO)
                    .build();

            // Salva o ingresso no banco de dados
            ingressoRepository.save(ingressoNovo);

            // Associa o ingresso ao lote
            lote.getIngressos().add(ingressoNovo);
        }

        // Salva o lote no banco de dados
        loteRepository.save(lote);

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
    @DisplayName("Gerando o relatório igual ao exemplo dado no documento")
    void testCompletoMesmoDoDocumento() throws Exception {
        //Act
        System.out.println(show);
        String responseJSONString = driver.perform(get(URI_RELATORIO + "/" + show.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        RelatorioShow relatorioShow = objectMapper.readValue(responseJSONString, RelatorioShow.class);

        //Assert
        assertEquals(StatusFinanceiro.LUCRO, relatorioShow.getStatus_financeiro());
        assertEquals(1625, relatorioShow.getReceita_liquida());
        assertEquals(100, relatorioShow.getIngressos_vip_vendidos());
        assertEquals(50, relatorioShow.getIngressos_meia_vendidos());
        assertEquals(350, relatorioShow.getIngressos_normal_vendidos());
    }

}

