package cl.patria.diputado_profile_service.controller;

import cl.patria.diputado_profile_service.model.ProfileEntity;
import cl.patria.diputado_profile_service.service.ProfileService;
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

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProfileService service;

    @InjectMocks
    private ProfileController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void returnProfiles_returns200WithCollectionModel_whenProfilesExist() throws Exception {
        ProfileEntity entity = new ProfileEntity(1, "Nombre Test", "Partido Test", 10, "Region Test", List.of("Comuna1"));
        when(service.getProfiles()).thenReturn(List.of(entity));

        mockMvc.perform(get("/api/v1/profiles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].nombreCompleto").value("Nombre Test"))
                .andExpect(jsonPath("$.links").exists());
    }

    @Test
    void returnProfiles_returns204_whenNoProfiles() throws Exception {
        when(service.getProfiles()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/profiles"))
                .andExpect(status().isNoContent());
    }

    @Test
    void returnProfiles_returns204_whenNull() throws Exception {
        when(service.getProfiles()).thenReturn(null);

        mockMvc.perform(get("/api/v1/profiles"))
                .andExpect(status().isNoContent());
    }

    @Test
    void returnProfileById_returns200WithEntityModel_whenProfileExists() throws Exception {
        ProfileEntity entity = new ProfileEntity(1, "Nombre Test", "Partido Test", 10, "Region Test", List.of("Comuna1"));
        when(service.getProfile(1)).thenReturn(Optional.of(entity));

        mockMvc.perform(get("/api/v1/profiles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombreCompleto").value("Nombre Test"))
                .andExpect(jsonPath("$.links").exists());
    }

    @Test
    void returnProfileById_returns204_whenProfileNotFound() throws Exception {
        when(service.getProfile(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/profiles/99"))
                .andExpect(status().isNoContent());
    }
}
