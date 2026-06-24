package cl.patria.controller;

import cl.patria.model.RegionEntity;
import cl.patria.service.RegionService;
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
class RegionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RegionService service;

    @InjectMocks
    private RegionController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void returnRegiones_returns200WithCollectionModel_whenRegionesExist() throws Exception {
        RegionEntity entity = new RegionEntity();
        entity.setId(1);
        entity.setNombre("Region Test");
        entity.setNumRomano("I");
        when(service.findAll()).thenReturn(List.of(entity));

        mockMvc.perform(get("/api/v1/scrap/regiones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].nombre").value("Region Test"))
                .andExpect(jsonPath("$.links").exists());
    }

    @Test
    void returnRegiones_returns204_whenEmptyList() throws Exception {
        when(service.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/scrap/regiones"))
                .andExpect(status().isNoContent());
    }

    @Test
    void returnRegion_returns200WithEntityModel_whenFound() throws Exception {
        RegionEntity entity = new RegionEntity();
        entity.setId(1);
        entity.setNombre("Region Test");
        entity.setNumRomano("I");
        when(service.findById(1)).thenReturn(Optional.of(entity));

        mockMvc.perform(get("/api/v1/scrap/regiones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Region Test"))
                .andExpect(jsonPath("$.links").exists());
    }

    @Test
    void returnRegion_returns404_whenNotFound() throws Exception {
        when(service.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/scrap/regiones/99"))
                .andExpect(status().isNotFound());
    }
}
