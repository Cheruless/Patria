package cl.patria.controller;

import cl.patria.model.ComunaEntity;
import cl.patria.service.ComunaService;
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
class ComunaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ComunaService service;

    @InjectMocks
    private ComunaController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void returnComunas_returns200WithCollectionModel_whenComunasExist() throws Exception {
        ComunaEntity entity = new ComunaEntity();
        entity.setId(1);
        entity.setNombre("Comuna Test");
        when(service.findAll()).thenReturn(List.of(entity));

        mockMvc.perform(get("/api/v1/scrap/comunas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].nombre").value("Comuna Test"))
                .andExpect(jsonPath("$.links").exists());
    }

    @Test
    void returnComunas_returns204_whenEmptyList() throws Exception {
        when(service.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/scrap/comunas"))
                .andExpect(status().isNoContent());
    }

    @Test
    void returnComuna_returns200WithEntityModel_whenFound() throws Exception {
        ComunaEntity entity = new ComunaEntity();
        entity.setId(1);
        entity.setNombre("Comuna Test");
        when(service.findById(1)).thenReturn(Optional.of(entity));

        mockMvc.perform(get("/api/v1/scrap/comunas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Comuna Test"))
                .andExpect(jsonPath("$.links").exists());
    }

    @Test
    void returnComuna_returns404_whenNotFound() throws Exception {
        when(service.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/scrap/comunas/99"))
                .andExpect(status().isNotFound());
    }
}
