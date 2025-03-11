package com.vandv.sistema_ingressos.test_show.junit5Tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vandv.sistema_ingressos.dto.ShowPostDto;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.stream.IntStream;

import static org.aspectj.bridge.MessageUtil.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Testes em relação ao relatório de um show")
public class AVLTests {
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
    @Nested
    @DisplayName("Testes de Quantidade de Ingressos")
    class TesteQuantidadeIngressos {
        @ParameterizedTest
        @CsvSource({"20,10,70", "21,10,69", "25,10,65", "29,10,61"})
        @DisplayName("Teste parametrizado de diferentes quantidades de ingressos")
        @Order(1)
        void testarDiferentesQuantidadesDeIngressos(int qtdVIP, int qtdMeia, int qtdNormal) throws Exception {
            criarShowComIngressos(qtdVIP, qtdMeia, qtdNormal);
        }
    }

    @DisplayName("Teste repetido para robustez")
    @RepeatedTest(3)
    @Timeout(5)
    void testeRepetidoParaRobustez() throws Exception {
        criarShowComIngressos(20, 10, 70);
    }

    private void criarShowComIngressos(int qtdVIP, int qtdMeia, int qtdNormal) throws Exception {
        // Dados para os testes
        Artista artista = artistaRepository.save(Artista.builder()
                .id(1L)
                .nome("Lucas")
                .sobrenome("Pereira")
                .nomeArtistico("Banda LuFi")
                .genero("sertanejo")
                .cpf("870.756.333-90")
                .build());

        ShowPostDto showPostDto = ShowPostDto.builder()
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(100000)
                .totalDespesas(30000)
                .lote(new ArrayList<>())
                .dataEspecial(true)
                .build();

        Lote loteX = loteRepository.save(Lote.builder()
                .ingressos(new ArrayList<>())
                .desconto(-1.0)
                .qtdIngressos(0)
                .build());

        showPostDto.getLote().add(loteX);

        Show show = showRepository.save(Show.builder()
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(100000)
                .totalDespesas(30000)
                .lote(new ArrayList<>())
                .dataEspecial(true)
                .build());

        adicionarIngressos(qtdVIP, TipoIngresso.VIP, 200.0, loteX);
        adicionarIngressos(qtdMeia, TipoIngresso.MEIA_ENTRADA, 50.0, loteX);
        adicionarIngressos(qtdNormal, TipoIngresso.NORMAL, 100.0, loteX);
    }

    private void adicionarIngressos(int quantidade, TipoIngresso tipo, double preco, Lote loteX) throws Exception {
        IntStream.range(0, quantidade).forEach(i -> {
            try {
                Ingresso ingresso1 = Ingresso.builder()
                        .preco(preco)
                        .tipo(tipo)
                        .status(StatusIngresso.VENDIDO)
                        .build();
                Ingresso ingresso = ingressoRepository.save(ingresso1);
                driver.perform(MockMvcRequestBuilders.patch(URI_INGRESSO + "/addIngresso/" + ingresso.getId() + "/" + loteX.getId())
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk());
            } catch (Exception e) {
                fail("Erro ao adicionar ingresso", e);
            }
        });
    }

    @Test
    @Order(1)
    @DisplayName("Minimo de ingressos Vip possiveis")
    void MinimoPermitidoDeIngressosVIP() throws Exception {
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
                .desconto(0.1)
                .qtdIngressos(100)
                .build());

        for (int i = 0; i < 20; i++) { // 20 VIP
            Ingresso ingressoX = ingressoRepository.save(Ingresso.builder()
                    .preco(200.0)
                    .tipo(TipoIngresso.VIP)
                    .status(StatusIngresso.VENDIDO)
                    .build());
            driver.perform(patch(URI_INGRESSO + "/addIngresso/" + ingressoX.getId() + "/" +loteX.getId() )
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        for (int i = 0; i < 10; i++) { // 10 MEIA_ENTRADA
            Ingresso ingresso = ingressoRepository.save(Ingresso.builder()
                    .preco(50.0) // Metade do NORMAL
                    .tipo(TipoIngresso.MEIA_ENTRADA)
                    .status(StatusIngresso.VENDIDO)
                    .build());

            driver.perform(patch(URI_INGRESSO + "/addIngresso/"  + ingresso.getId() + "/" +loteX.getId() )
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        for (int i = 0; i < 70; i++) { // 70 NORMAL
            Ingresso ingresso = ingressoRepository.save(Ingresso.builder()
                    .preco(100.0)
                    .tipo(TipoIngresso.NORMAL)
                    .status(StatusIngresso.VENDIDO)
                    .build());

            driver.perform(patch(URI_INGRESSO + "/addIngresso/" +  + ingresso.getId() + "/" +loteX.getId() )
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        System.out.println("A quaantidade de ingressos é: " + loteX.getIngressos().size());
    }

    @Test
    @Order(2)
    @DisplayName("Logo acima do permitido de ingressos Vip possiveis")
    void LogoAcimaMinimoPermitidoDeIngressosVIP() throws Exception {
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
                .desconto(0.1)
                .qtdIngressos(100)
                .build());

        for (int i = 0; i < 21; i++) { // 20 VIP
            Ingresso ingressoX = ingressoRepository.save(Ingresso.builder()
                    .preco(200.0)
                    .tipo(TipoIngresso.VIP)
                    .status(StatusIngresso.VENDIDO)
                    .build());
            driver.perform(patch(URI_INGRESSO + "/addIngresso/" + ingressoX.getId() + "/" +loteX.getId() )
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        for (int i = 0; i < 10; i++) { // 10 MEIA_ENTRADA
            Ingresso ingresso = ingressoRepository.save(Ingresso.builder()
                    .preco(50.0) // Metade do NORMAL
                    .tipo(TipoIngresso.MEIA_ENTRADA)
                    .status(StatusIngresso.VENDIDO)
                    .build());

            driver.perform(patch(URI_INGRESSO + "/addIngresso/"  + ingresso.getId() + "/" +loteX.getId() )
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        for (int i = 0; i < 69; i++) { // 70 NORMAL
            Ingresso ingresso = ingressoRepository.save(Ingresso.builder()
                    .preco(100.0)
                    .tipo(TipoIngresso.NORMAL)
                    .status(StatusIngresso.VENDIDO)
                    .build());

            driver.perform(patch(URI_INGRESSO + "/addIngresso/" +  + ingresso.getId() + "/" +loteX.getId() )
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        System.out.println("A quaantidade de ingressos é: " + loteX.getIngressos().size());
    }
    @Test
    @Order(3)
    @DisplayName("Valor medio de ingressos Vip possiveis")
    void ValorMediooDeIngressosVIP() throws Exception {
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
                .desconto(0.1)
                .qtdIngressos(100)
                .build());

        for (int i = 0; i < 25; i++) { // 25 VIP
            Ingresso ingressoX = ingressoRepository.save(Ingresso.builder()
                    .preco(200.0)
                    .tipo(TipoIngresso.VIP)
                    .status(StatusIngresso.VENDIDO)
                    .build());
            driver.perform(patch(URI_INGRESSO + "/addIngresso/" + ingressoX.getId() + "/" +loteX.getId() )
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        for (int i = 0; i < 10; i++) { // 10 MEIA_ENTRADA
            Ingresso ingresso = ingressoRepository.save(Ingresso.builder()
                    .preco(50.0) // Metade do NORMAL
                    .tipo(TipoIngresso.MEIA_ENTRADA)
                    .status(StatusIngresso.VENDIDO)
                    .build());

            driver.perform(patch(URI_INGRESSO + "/addIngresso/"  + ingresso.getId() + "/" +loteX.getId() )
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        for (int i = 0; i < 65; i++) { // 65 NORMAL
            Ingresso ingresso = ingressoRepository.save(Ingresso.builder()
                    .preco(100.0)
                    .tipo(TipoIngresso.NORMAL)
                    .status(StatusIngresso.VENDIDO)
                    .build());

            driver.perform(patch(URI_INGRESSO + "/addIngresso/" +  + ingresso.getId() + "/" +loteX.getId() )
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        System.out.println("A quaantidade de ingressos é: " + loteX.getIngressos().size());
    }

    @Test
    @Order(4)
    @DisplayName("Logo abaixo do máximo")
    void LogoAbaixodoMaximoPermitidoDeIngressosVIP() throws Exception {
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
                .desconto(0.1)
                .qtdIngressos(100)
                .build());

        for (int i = 0; i < 29; i++) { // 29 VIP
            Ingresso ingressoX = ingressoRepository.save(Ingresso.builder()
                    .preco(200.0)
                    .tipo(TipoIngresso.VIP)
                    .status(StatusIngresso.VENDIDO)
                    .build());
            driver.perform(patch(URI_INGRESSO + "/addIngresso/" + ingressoX.getId() + "/" +loteX.getId() )
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        for (int i = 0; i < 10; i++) { // 10 MEIA_ENTRADA
            Ingresso ingresso = ingressoRepository.save(Ingresso.builder()
                    .preco(50.0) // Metade do NORMAL
                    .tipo(TipoIngresso.MEIA_ENTRADA)
                    .status(StatusIngresso.VENDIDO)
                    .build());

            driver.perform(patch(URI_INGRESSO + "/addIngresso/"  + ingresso.getId() + "/" +loteX.getId() )
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        for (int i = 0; i < 61; i++) { // 61 NORMAL
            Ingresso ingresso = ingressoRepository.save(Ingresso.builder()
                    .preco(100.0)
                    .tipo(TipoIngresso.NORMAL)
                    .status(StatusIngresso.VENDIDO)
                    .build());

            driver.perform(patch(URI_INGRESSO + "/addIngresso/" +  + ingresso.getId() + "/" +loteX.getId() )
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        System.out.println("A quaantidade de ingressos é: " + loteX.getIngressos().size());
    }

    @Test
    @Order(5)
    @DisplayName("Maximo de ingressos Vip possiveis")
    void MaximoPermitidoDeIngressosVIP() throws Exception {
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
                .desconto(0.1)
                .qtdIngressos(100)
                .build());

        for (int i = 0; i < 30; i++) { // 30 VIP
            Ingresso ingressoX = ingressoRepository.save(Ingresso.builder()
                    .preco(200.0)
                    .tipo(TipoIngresso.VIP)
                    .status(StatusIngresso.VENDIDO)
                    .build());
            driver.perform(patch(URI_INGRESSO + "/addIngresso/" + ingressoX.getId() + "/" +loteX.getId() )
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        for (int i = 0; i < 10; i++) { // 10 MEIA_ENTRADA
            Ingresso ingresso = ingressoRepository.save(Ingresso.builder()
                    .preco(50.0) // Metade do NORMAL
                    .tipo(TipoIngresso.MEIA_ENTRADA)
                    .status(StatusIngresso.VENDIDO)
                    .build());

            driver.perform(patch(URI_INGRESSO + "/addIngresso/"  + ingresso.getId() + "/" +loteX.getId() )
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        for (int i = 0; i < 60; i++) { // 60 NORMAL
            Ingresso ingresso = ingressoRepository.save(Ingresso.builder()
                    .preco(100.0)
                    .tipo(TipoIngresso.NORMAL)
                    .status(StatusIngresso.VENDIDO)
                    .build());

            driver.perform(patch(URI_INGRESSO + "/addIngresso/" +  + ingresso.getId() + "/" +loteX.getId() )
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        System.out.println("A quaantidade de ingressos é: " + loteX.getIngressos().size());
    }

    @Test
    @Order(6)
    @DisplayName("Acima do máximo permitido de ingressos Vip possiveis")
    void AcimaDoMaximoPermitidoDeIngressosVIP() throws Exception {
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
                .desconto(0.1)
                .qtdIngressos(100)
                .build());

        for (int i = 0; i < 31; i++) { // 31 VIP
            Ingresso ingressoX = ingressoRepository.save(Ingresso.builder()
                    .preco(200.0)
                    .tipo(TipoIngresso.VIP)
                    .status(StatusIngresso.VENDIDO)
                    .build());
            driver.perform(patch(URI_INGRESSO + "/addIngresso/" + ingressoX.getId() + "/" +loteX.getId() )
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        for (int i = 0; i < 10; i++) { // 10 MEIA_ENTRADA
            Ingresso ingresso = ingressoRepository.save(Ingresso.builder()
                    .preco(50.0) // Metade do NORMAL
                    .tipo(TipoIngresso.MEIA_ENTRADA)
                    .status(StatusIngresso.VENDIDO)
                    .build());

            driver.perform(patch(URI_INGRESSO + "/addIngresso/"  + ingresso.getId() + "/" +loteX.getId() )
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        for (int i = 0; i < 59; i++) { // 59 NORMAL
            Ingresso ingresso = ingressoRepository.save(Ingresso.builder()
                    .preco(100.0)
                    .tipo(TipoIngresso.NORMAL)
                    .status(StatusIngresso.VENDIDO)
                    .build());

            driver.perform(patch(URI_INGRESSO + "/addIngresso/" +  + ingresso.getId() + "/" +loteX.getId() )
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        System.out.println("A quaantidade de ingressos é: " + loteX.getIngressos().size());
    }

    @Test
    @Order(7)
    @DisplayName("Abaixo do mínimo permitido de ingressos Vip possiveis")
    void AbaixoDoMinimoPermitidoDeIngressosVIP() throws Exception {
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
                .desconto(0.1)
                .qtdIngressos(100)
                .build());

        for (int i = 0; i < 19; i++) { // 19 VIP
            Ingresso ingressoX = ingressoRepository.save(Ingresso.builder()
                    .preco(200.0)
                    .tipo(TipoIngresso.VIP)
                    .status(StatusIngresso.VENDIDO)
                    .build());
            driver.perform(patch(URI_INGRESSO + "/addIngresso/" + ingressoX.getId() + "/" +loteX.getId() )
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        for (int i = 0; i < 10; i++) { // 10 MEIA_ENTRADA
            Ingresso ingresso = ingressoRepository.save(Ingresso.builder()
                    .preco(50.0) // Metade do NORMAL
                    .tipo(TipoIngresso.MEIA_ENTRADA)
                    .status(StatusIngresso.VENDIDO)
                    .build());

            driver.perform(patch(URI_INGRESSO + "/addIngresso/"  + ingresso.getId() + "/" +loteX.getId() )
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        for (int i = 0; i < 71; i++) { // 71 NORMAL
            Ingresso ingresso = ingressoRepository.save(Ingresso.builder()
                    .preco(100.0)
                    .tipo(TipoIngresso.NORMAL)
                    .status(StatusIngresso.VENDIDO)
                    .build());

            driver.perform(patch(URI_INGRESSO + "/addIngresso/" +  + ingresso.getId() + "/" +loteX.getId() )
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        System.out.println("A quantidade de ingressos é: " + loteX.getIngressos().size());
    }

    @Test
    @Order(8)
    @DisplayName("Criando um show com 0 ingressos")
    void CriarshowCom0Ingressos() throws Exception {
        // Dados para os testes
        Artista artista = artistaRepository.save(Artista.builder()
                .id(1L)
                .nome("Lucas")
                .sobrenome("Pereira")
                .nomeArtistico("Banda LuFi")
                .genero("sertanejo")
                .cpf("870.756.333-90")
                .build());

        ShowPostDto showPostDto = ShowPostDto.builder()
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(100000)
                .totalDespesas(30000)
                .lote(new ArrayList<>())
                .dataEspecial(true)
                .build();

        Lote loteX = loteRepository.save(Lote.builder()
                .ingressos(new ArrayList<>())
                .desconto(0.1)
                .qtdIngressos(0)
                .build());

        showPostDto.getLote().add(loteX);

        Show show = showRepository.save(Show.builder()
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(100000)
                .totalDespesas(30000)
                .lote(new ArrayList<>())
                .dataEspecial(true)
                .build());


        //Act
        String responseJSONString = driver.perform(patch(URI_SHOW + "/addLote/" + show.getId() + "/" + loteX.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

    }

    @Test
    @Order(9)
    @DisplayName("Criando um show com 1 ingressos")
    void CriarshowCom1Ingressos() throws Exception {
        // Dados para os testes
        Artista artista = artistaRepository.save(Artista.builder()
                .id(1L)
                .nome("Lucas")
                .sobrenome("Pereira")
                .nomeArtistico("Banda LuFi")
                .genero("sertanejo")
                .cpf("870.756.333-90")
                .build());

        ShowPostDto showPostDto = ShowPostDto.builder()
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(100000)
                .totalDespesas(30000)
                .lote(new ArrayList<>())
                .dataEspecial(true)
                .build();

        Lote loteX = loteRepository.save(Lote.builder()
                .ingressos(new ArrayList<>())
                .desconto(0.1)
                .qtdIngressos(1)
                .build());

        showPostDto.getLote().add(loteX);

        Show show = showRepository.save(Show.builder()
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(100000)
                .totalDespesas(30000)
                .lote(new ArrayList<>())
                .dataEspecial(true)
                .build());


        //Act
        String responseJSONString = driver.perform(patch(URI_SHOW + "/addLote/" + show.getId() + "/" + loteX.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

    }

    @Test
    @Order(10)
    @DisplayName("Criando um show com 2000 ingressos")
    void CriarshowCom2000Ingressos() throws Exception {
        // Dados para os testes
        Artista artista = artistaRepository.save(Artista.builder()
                .id(1L)
                .nome("Lucas")
                .sobrenome("Pereira")
                .nomeArtistico("Banda LuFi")
                .genero("sertanejo")
                .cpf("870.756.333-90")
                .build());

        ShowPostDto showPostDto = ShowPostDto.builder()
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(100000)
                .totalDespesas(30000)
                .lote(new ArrayList<>())
                .dataEspecial(true)
                .build();

        Lote loteX = loteRepository.save(Lote.builder()
                .ingressos(new ArrayList<>())
                .desconto(0.1)
                .qtdIngressos(2000)
                .build());

        showPostDto.getLote().add(loteX);

        Show show = showRepository.save(Show.builder()
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(100000)
                .totalDespesas(30000)
                .lote(new ArrayList<>())
                .dataEspecial(true)
                .build());


        //Act
        String responseJSONString = driver.perform(patch(URI_SHOW + "/addLote/" + show.getId() + "/" + loteX.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

    }

    @Test
    @Order(11)
    @DisplayName("Criando um show com 2 ingressos")
    void CriarshowCom2Ingressos() throws Exception {
        // Dados para os testes
        Artista artista = artistaRepository.save(Artista.builder()
                .id(1L)
                .nome("Lucas")
                .sobrenome("Pereira")
                .nomeArtistico("Banda LuFi")
                .genero("sertanejo")
                .cpf("870.756.333-90")
                .build());

        ShowPostDto showPostDto = ShowPostDto.builder()
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(100000)
                .totalDespesas(30000)
                .lote(new ArrayList<>())
                .dataEspecial(true)
                .build();

        Lote loteX = loteRepository.save(Lote.builder()
                .ingressos(new ArrayList<>())
                .desconto(0.1)
                .qtdIngressos(0)
                .build());

        showPostDto.getLote().add(loteX);

        Show show = showRepository.save(Show.builder()
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(100000)
                .totalDespesas(30000)
                .lote(new ArrayList<>())
                .dataEspecial(true)
                .build());


        //Act
        String responseJSONString = driver.perform(patch(URI_SHOW + "/addLote/" + show.getId() + "/" + loteX.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

    }

    @Test
    @Order(12)
    @DisplayName("Desconto minimo")
    void DscontoMinimoPermitido() throws Exception {
        // Dados para os testes
        Artista artista = artistaRepository.save(Artista.builder()
                .id(1L)
                .nome("Lucas")
                .sobrenome("Pereira")
                .nomeArtistico("Banda LuFi")
                .genero("sertanejo")
                .cpf("870.756.333-90")
                .build());

        ShowPostDto showPostDto = ShowPostDto.builder()
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(100000)
                .totalDespesas(30000)
                .lote(new ArrayList<>())
                .dataEspecial(true)
                .build();

        Lote loteX = loteRepository.save(Lote.builder()
                .ingressos(new ArrayList<>())
                .desconto(0.0)
                .qtdIngressos(0)
                .build());

        showPostDto.getLote().add(loteX);

        Show show = showRepository.save(Show.builder()
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(100000)
                .totalDespesas(30000)
                .lote(new ArrayList<>())
                .dataEspecial(true)
                .build());


        //Act
        String responseJSONString = driver.perform(patch(URI_SHOW + "/addLote/" + show.getId() + "/" + loteX.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

    }

    @Test
    @Order(13)
    @DisplayName("Logo acima do Desconto minimo")
    void LogoAcimaDescontoMinimoPermitido() throws Exception {
        // Dados para os testes
        Artista artista = artistaRepository.save(Artista.builder()
                .id(1L)
                .nome("Lucas")
                .sobrenome("Pereira")
                .nomeArtistico("Banda LuFi")
                .genero("sertanejo")
                .cpf("870.756.333-90")
                .build());

        ShowPostDto showPostDto = ShowPostDto.builder()
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(100000)
                .totalDespesas(30000)
                .lote(new ArrayList<>())
                .dataEspecial(true)
                .build();

        Lote loteX = loteRepository.save(Lote.builder()
                .ingressos(new ArrayList<>())
                .desconto(0.01)
                .qtdIngressos(0)
                .build());

        showPostDto.getLote().add(loteX);

        Show show = showRepository.save(Show.builder()
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(100000)
                .totalDespesas(30000)
                .lote(new ArrayList<>())
                .dataEspecial(true)
                .build());


        //Act
        String responseJSONString = driver.perform(patch(URI_SHOW + "/addLote/" + show.getId() + "/" + loteX.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

    }

    @Test
    @Order(14)
    @DisplayName("Valor Médio de desconto")
    void ValorMedioDescontoPermitido() throws Exception {
        // Dados para os testes
        Artista artista = artistaRepository.save(Artista.builder()
                .id(1L)
                .nome("Lucas")
                .sobrenome("Pereira")
                .nomeArtistico("Banda LuFi")
                .genero("sertanejo")
                .cpf("870.756.333-90")
                .build());

        ShowPostDto showPostDto = ShowPostDto.builder()
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(100000)
                .totalDespesas(30000)
                .lote(new ArrayList<>())
                .dataEspecial(true)
                .build();

        Lote loteX = loteRepository.save(Lote.builder()
                .ingressos(new ArrayList<>())
                .desconto(10.0)
                .qtdIngressos(0)
                .build());

        showPostDto.getLote().add(loteX);

        Show show = showRepository.save(Show.builder()
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(100000)
                .totalDespesas(30000)
                .lote(new ArrayList<>())
                .dataEspecial(true)
                .build());


        //Act
        String responseJSONString = driver.perform(patch(URI_SHOW + "/addLote/" + show.getId() + "/" + loteX.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

    }

    @Test
    @Order(15)
    @DisplayName("Logo abaixo do máximo de desconto")
    void LogoAbaixoDoMaximoPermitidoDesconto() throws Exception {
        // Dados para os testes
        Artista artista = artistaRepository.save(Artista.builder()
                .id(1L)
                .nome("Lucas")
                .sobrenome("Pereira")
                .nomeArtistico("Banda LuFi")
                .genero("sertanejo")
                .cpf("870.756.333-90")
                .build());

        ShowPostDto showPostDto = ShowPostDto.builder()
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(100000)
                .totalDespesas(30000)
                .lote(new ArrayList<>())
                .dataEspecial(true)
                .build();

        Lote loteX = loteRepository.save(Lote.builder()
                .ingressos(new ArrayList<>())
                .desconto(24.99)
                .qtdIngressos(0)
                .build());

        showPostDto.getLote().add(loteX);

        Show show = showRepository.save(Show.builder()
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(100000)
                .totalDespesas(30000)
                .lote(new ArrayList<>())
                .dataEspecial(true)
                .build());


        //Act
        String responseJSONString = driver.perform(patch(URI_SHOW + "/addLote/" + show.getId() + "/" + loteX.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

    }

    @Test
    @Order(16)
    @DisplayName("Desconto máximo permitido")
    void DescontoMaximoPermitido() throws Exception {
        // Dados para os testes
        Artista artista = artistaRepository.save(Artista.builder()
                .id(1L)
                .nome("Lucas")
                .sobrenome("Pereira")
                .nomeArtistico("Banda LuFi")
                .genero("sertanejo")
                .cpf("870.756.333-90")
                .build());

        ShowPostDto showPostDto = ShowPostDto.builder()
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(100000)
                .totalDespesas(30000)
                .lote(new ArrayList<>())
                .dataEspecial(true)
                .build();

        Lote loteX = loteRepository.save(Lote.builder()
                .ingressos(new ArrayList<>())
                .desconto(25.00)
                .qtdIngressos(0)
                .build());

        showPostDto.getLote().add(loteX);

        Show show = showRepository.save(Show.builder()
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(100000)
                .totalDespesas(30000)
                .lote(new ArrayList<>())
                .dataEspecial(true)
                .build());


        //Act
        String responseJSONString = driver.perform(patch(URI_SHOW + "/addLote/" + show.getId() + "/" + loteX.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

    }

    @Test
    @Order(17)
    @DisplayName("Abaixo do Minimo de descontoPermitido")
    void AbaixoDoMinimoDeDescontoPermitido() throws Exception {
        // Dados para os testes
        Artista artista = artistaRepository.save(Artista.builder()
                .id(1L)
                .nome("Lucas")
                .sobrenome("Pereira")
                .nomeArtistico("Banda LuFi")
                .genero("sertanejo")
                .cpf("870.756.333-90")
                .build());

        ShowPostDto showPostDto = ShowPostDto.builder()
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(100000)
                .totalDespesas(30000)
                .lote(new ArrayList<>())
                .dataEspecial(true)
                .build();

        Lote loteX = loteRepository.save(Lote.builder()
                .ingressos(new ArrayList<>())
                .desconto(-1.0)
                .qtdIngressos(0)
                .build());

        showPostDto.getLote().add(loteX);

        Show show = showRepository.save(Show.builder()
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(100000)
                .totalDespesas(30000)
                .lote(new ArrayList<>())
                .dataEspecial(true)
                .build());


        //Act
        String responseJSONString = driver.perform(patch(URI_SHOW + "/addLote/" + show.getId() + "/" + loteX.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

    }

    @Test
    @Order(18)
    @DisplayName("Acima do desconto Máximo Permitido")
    void AcimaDoDescontoMaximoPermitido() throws Exception {
        // Dados para os testes
        Artista artista = artistaRepository.save(Artista.builder()
                .id(1L)
                .nome("Lucas")
                .sobrenome("Pereira")
                .nomeArtistico("Banda LuFi")
                .genero("sertanejo")
                .cpf("870.756.333-90")
                .build());

        ShowPostDto showPostDto = ShowPostDto.builder()
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(100000)
                .totalDespesas(30000)
                .lote(new ArrayList<>())
                .dataEspecial(true)
                .build();

        Lote loteX = loteRepository.save(Lote.builder()
                .ingressos(new ArrayList<>())
                .desconto(25.01)
                .qtdIngressos(0)
                .build());

        showPostDto.getLote().add(loteX);

        Show show = showRepository.save(Show.builder()
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(100000)
                .totalDespesas(30000)
                .lote(new ArrayList<>())
                .dataEspecial(true)
                .build());


        //Act
        String responseJSONString = driver.perform(patch(URI_SHOW + "/addLote/" + show.getId() + "/" + loteX.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

    }


}

