package cl.patria.controller;

import cl.patria.model.DiputadoEntity;
import cl.patria.service.DiputadoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DiputadoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DiputadoService service;

    @InjectMocks
    private DiputadoController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void returnDiputados_returns200WithCollectionModel_whenDiputadosExist() throws Exception {
        DiputadoEntity entity = new DiputadoEntity();
        entity.setDiputado_id(1);
        entity.setNombreCompleto("Diputado Test");
        entity.setAliasPartido("Partido Test");
        when(service.getDiputados()).thenReturn(List.of(entity));

        mockMvc.perform(get("/api/v1/scrap/diputados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content[0].diputado_id").value(1))
                .andExpect(jsonPath("$.content[0].nombreCompleto").value("Diputado Test"))
                .andExpect(jsonPath("$.links").exists());
    }

    @Test
    void returnDiputados_returns204_whenEmptyList() throws Exception {
        when(service.getDiputados()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/scrap/diputados"))
                .andExpect(status().isNoContent());
    }

    @Test
    void returnDiputados_returns204_whenNull() throws Exception {
        when(service.getDiputados()).thenReturn(null);

        mockMvc.perform(get("/api/v1/scrap/diputados"))
                .andExpect(status().isNoContent());
    }

    @Test
    void returnDiputadoById_returns200WithEntityModel_whenFound() throws Exception {
        DiputadoEntity entity = new DiputadoEntity();
        entity.setDiputado_id(1);
        entity.setNombreCompleto("Diputado Test");
        entity.setAliasPartido("Partido Test");
        when(service.getDiputado(1)).thenReturn(Optional.of(entity));

        mockMvc.perform(get("/api/v1/scrap/diputados/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diputado_id").value(1))
                .andExpect(jsonPath("$.nombreCompleto").value("Diputado Test"))
                .andExpect(jsonPath("$.links").exists());
    }

    @Test
    void returnDiputadoById_returns404_whenNotFound() throws Exception {
        when(service.getDiputado(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/scrap/diputados/99"))
                .andExpect(status().isNotFound());
    }
}
