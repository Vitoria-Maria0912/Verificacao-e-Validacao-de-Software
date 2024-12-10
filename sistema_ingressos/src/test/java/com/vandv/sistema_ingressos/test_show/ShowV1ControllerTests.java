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
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Date;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Testes em relação as funcionalidades de um show")
public class ShowV1ControllerTests {
    final String URI_SHOW = "/v1/show";
    @Autowired
    ShowRepository showRepository;
    @Autowired
    ArtistaRepository artistaRepository;
    @Autowired
    LoteRepository loteRepository;

    @Autowired
    MockMvc driver;
    ObjectMapper objectMapper = new ObjectMapper();
    ModelMapper modelMapper = new ModelMapper();

    Show show;
    Show show_one;
    Artista artista;
    Lote lote;
    ShowPostDto showPostDto;
    @BeforeEach
    void setup() {
        objectMapper.registerModule(new JavaTimeModule());


        artista = artistaRepository.save(Artista.builder()
                .id(1L)
                .nome("Lucas")
                .sobrenome("Pereira")
                .nomeArtistico("Banda LuFi")
                .genero("sertanejo")
                .cpf("870.756.333-90")
                .build());

        showPostDto = ShowPostDto.builder()
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(100000)
                .totalDespesas(30000)
                .lote(new ArrayList<>())
                .dataEspecial(true)
                .build();

        show =  showRepository.save(Show.builder()
                .id(1L)
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(100000)
                .totalDespesas(30000)
                .lote(new ArrayList<>())
                .dataEspecial(true)
                .build());

        show_one =  showRepository.save(Show.builder()
                .id(2L)
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
    }

    @Test
    @DisplayName("Criando um show")
    void testeAoCriarShow() throws Exception {
        // Arrange
        // nenhuma necessidade além do setup()

        // Act
        String responseJSONString = driver.perform(post(URI_SHOW + "/criar-show")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(showPostDto)))
                .andExpect(status().isCreated())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        Show show = objectMapper.readValue(responseJSONString, Show.class);

        // Assert
        assertEquals(100000, show.getCache());
        assertEquals(true, show.getDataEspecial());
    }

    @Test
    @DisplayName("Quando busco todos os shows salvos")
    void testAoBuscarTodosShow() throws Exception {
        //Arrange
        ShowPostDto showPostDto = ShowPostDto.builder()
                .dataShow(new Date("12/03/2023"))
                .artista(artista)
                .cache(100000)
                .totalDespesas(30000)
                .lote(new ArrayList<>())
                .dataEspecial(true)
                .build();
        Show show1 = modelMapper.map(showPostDto, Show.class);
        ShowPostDto showPostDto_1 = ShowPostDto.builder()
                .dataShow(new Date("12/03/2025"))
                .artista(artista)
                .cache(500000)
                .totalDespesas(50000)
                .lote(new ArrayList<>())
                .dataEspecial(true)
                .build();
        Show show2 = modelMapper.map(showPostDto_1, Show.class);
        showRepository.save(show1);
        showRepository.save(show2);
        //Act
        String responseJSONString = driver.perform(get(URI_SHOW)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        List<Show> shows = objectMapper.readValue(responseJSONString, new TypeReference<List<Show>>(){});
        //Assert
        assertEquals(2, shows.size());
    }

    @Test
    @DisplayName("Quando busco um show pelo id")
    void testQuandoBuscoUmShow() throws Exception {
        //Act
        String responseJSONString = driver.perform(get(URI_SHOW + "/" + show.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        Show show = objectMapper.readValue(responseJSONString, Show.class);
        //Assert
        assertEquals(100000, show.getCache());
    }

    @Test
    @DisplayName("Quando deleto um show pelo id")
    void testQuandoDeletoShow() throws Exception{
        //Act
        String responseJSONString = driver.perform(delete(URI_SHOW + "/" + show.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        //Assert
        assertEquals(0, showRepository.findAll().size());
    }
    
}