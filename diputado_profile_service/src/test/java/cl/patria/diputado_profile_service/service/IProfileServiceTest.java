package cl.patria.diputado_profile_service.service;

import cl.patria.diputado_profile_service.model.ProfileEntity;
import cl.patria.diputado_profile_service.repository.ProfileRepository;
import cl.patria.diputado_profile_service.service.impl.IProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IProfileServiceTest {

    @Mock
    private ProfileRepository repository;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private IProfileService service;

    @Captor
    private ArgumentCaptor<List<ProfileEntity>> entityListCaptor;

    @BeforeEach
    void setUp() throws Exception {
        service = new IProfileService(webClient);
        ReflectionTestUtils.setField(service, "repository", repository);
        resetStaticFlags();
    }

    private void resetStaticFlags() throws Exception {
        setStaticFlag("savedFlag", false);
        setStaticFlag("webFlag", false);
    }

    private void setStaticFlag(String fieldName, boolean value) throws Exception {
        Field field = IProfileService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, value);
    }

    private void mockWebClientChain() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void getProfiles_shouldFetchAndSaveOnFirstCall_thenReturnFromRepository() {
        String jsonDiputados = """
                [
                  {
                    "diputado_id": 1,
                    "nombreCompleto": "Diputado Uno",
                    "aliasPartido": "Partido A",
                    "distritoNum": 10
                  }
                ]
                """;
        String jsonDistritos = """
                {
                  "_embedded": {
                    "distritoDTOList": [
                      {
                        "id": 10,
                        "region": { "id": 1, "nombre": "Region Test", "numRomano": "I" },
                        "comunas": [
                          { "id": 1, "nombre": "Comuna1" }
                        ]
                      }
                    ]
                  }
                }
                """;
        mockWebClientChain();
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.just(jsonDiputados))
                .thenReturn(Mono.just(jsonDistritos));

        ProfileEntity expectedEntity = new ProfileEntity(
                1, "Diputado Uno", "Partido A", 10, "Region Test", List.of("Comuna1")
        );
        when(repository.saveAll(entityListCaptor.capture())).thenReturn(List.of(expectedEntity));
        when(repository.findAll()).thenReturn(List.of(expectedEntity));

        List<ProfileEntity> result = service.getProfiles();

        assertThat(result).hasSize(1);
        ProfileEntity actual = result.getFirst();
        assertThat(actual.getId()).isEqualTo(1);
        assertThat(actual.getNombreCompleto()).isEqualTo("Diputado Uno");
        assertThat(actual.getPartido()).isEqualTo("Partido A");
        assertThat(actual.getDistritoId()).isEqualTo(10);
        assertThat(actual.getRegion()).isEqualTo("Region Test");
        assertThat(actual.getComunas()).containsExactly("Comuna1");

        List<ProfileEntity> savedEntities = entityListCaptor.getValue();
        assertThat(savedEntities).hasSize(1);
        assertThat(savedEntities.getFirst().getId()).isEqualTo(1);

        verify(repository).saveAll(any());
        verify(repository).flush();
        verify(repository).findAll();
        verify(webClient, times(2)).get();
    }

    @Test
    void getProfiles_shouldReturnFromRepository_whenAlreadySaved() throws Exception {
        setStaticFlag("savedFlag", true);

        ProfileEntity entity = new ProfileEntity(1, "Cached", "Partido", 5, "Region", List.of("Comuna"));
        when(repository.findAll()).thenReturn(List.of(entity));

        List<ProfileEntity> result = service.getProfiles();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getNombreCompleto()).isEqualTo("Cached");

        verify(repository).findAll();
        verify(repository, never()).saveAll(any());
        verify(repository, never()).flush();
        verifyNoInteractions(webClient);
    }

    @Test
    void getProfiles_shouldReturnEmptyList_whenWebClientFails() {
        mockWebClientChain();
        when(requestHeadersSpec.retrieve()).thenThrow(new RuntimeException("Connection refused"));
        when(repository.findAll()).thenReturn(List.of());

        List<ProfileEntity> result = service.getProfiles();

        assertThat(result).isEmpty();
        verify(repository, never()).saveAll(any());
        verify(repository).findAll();
    }

    @Test
    void getProfiles_shouldReturnEmptyList_whenExternalDataIsNull() {
        mockWebClientChain();
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.just(""))
                .thenReturn(Mono.just(""));
        when(repository.findAll()).thenReturn(List.of());

        List<ProfileEntity> result = service.getProfiles();

        assertThat(result).isEmpty();
        verify(repository, never()).saveAll(any());
        verify(repository).findAll();
    }

    @Test
    void getProfiles_shouldReturnEmptyList_whenExternalDataHasNoContent() {
        mockWebClientChain();
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.just("{}"))
                .thenReturn(Mono.just("{}"));
        when(repository.findAll()).thenReturn(List.of());

        List<ProfileEntity> result = service.getProfiles();

        assertThat(result).isEmpty();
        verify(repository, never()).saveAll(any());
        verify(repository).findAll();
    }

    @Test
    void getProfile_shouldReturnProfile_whenFound() {
        String jsonDiputados = """
                [
                  {
                    "diputado_id": 1,
                    "nombreCompleto": "Diputado Uno",
                    "aliasPartido": "Partido A",
                    "distritoNum": 10
                  }
                ]
                """;
        String jsonDistritos = """
                {
                  "_embedded": {
                    "distritoDTOList": [
                      {
                        "id": 10,
                        "region": { "id": 1, "nombre": "Region Test", "numRomano": "I" },
                        "comunas": [
                          { "id": 1, "nombre": "Comuna1" }
                        ]
                      }
                    ]
                  }
                }
                """;
        mockWebClientChain();
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.just(jsonDiputados))
                .thenReturn(Mono.just(jsonDistritos));

        ProfileEntity expectedEntity = new ProfileEntity(
                1, "Diputado Uno", "Partido A", 10, "Region Test", List.of("Comuna1")
        );
        when(repository.saveAll(any())).thenReturn(List.of(expectedEntity));
        when(repository.findById(1)).thenReturn(Optional.of(expectedEntity));

        Optional<ProfileEntity> result = service.getProfile(1);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1);
        assertThat(result.get().getNombreCompleto()).isEqualTo("Diputado Uno");
        verify(repository).saveAll(any());
        verify(repository).flush();
        verify(repository).findById(1);
    }

    @Test
    void getProfile_shouldReturnEmpty_whenNotFound() {
        String jsonDiputados = """
                [
                  {
                    "diputado_id": 99,
                    "nombreCompleto": "Diputado NoDb",
                    "aliasPartido": "Partido Z",
                    "distritoNum": 20
                  }
                ]
                """;
        String jsonDistritos = """
                {
                  "_embedded": {
                    "distritoDTOList": []
                  }
                }
                """;
        mockWebClientChain();
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.just(jsonDiputados))
                .thenReturn(Mono.just(jsonDistritos));

        when(repository.findById(99)).thenReturn(Optional.empty());

        Optional<ProfileEntity> result = service.getProfile(99);

        assertThat(result).isEmpty();
        verify(repository).findById(99);
    }

    @Test
    void getProfile_shouldReturnEmpty_whenExternalServiceFails() {
        mockWebClientChain();
        when(requestHeadersSpec.retrieve()).thenThrow(new RuntimeException("Connection refused"));
        when(repository.findById(1)).thenReturn(Optional.empty());

        Optional<ProfileEntity> result = service.getProfile(1);

        assertThat(result).isEmpty();
        verify(repository, never()).saveAll(any());
        verify(repository).findById(1);
    }
}
