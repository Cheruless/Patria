package cl.patria.service;

import cl.patria.model.ComunaEntity;
import cl.patria.model.DistritoEntity;
import cl.patria.model.RegionEntity;
import cl.patria.repository.ComunaRepository;
import cl.patria.repository.RegionRepository;
import cl.patria.service.impl.IRegionService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IRegionServiceTest {

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private ComunaRepository comunaRepository;

    @Mock
    private DistritoService distritoService;

    @Mock
    private EntityManager entityManager;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private IRegionService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new IRegionService(webClient);
        ReflectionTestUtils.setField(service, "regionRepository", regionRepository);
        ReflectionTestUtils.setField(service, "comunaRepository", comunaRepository);
        ReflectionTestUtils.setField(service, "distritoService", distritoService);
        ReflectionTestUtils.setField(service, "entityManager", entityManager);
        resetStaticFlag();
    }

    private void resetStaticFlag() throws Exception {
        Field field = IRegionService.class.getDeclaredField("savedFlag");
        field.setAccessible(true);
        field.set(null, false);
    }

    private void mockWebClientForSuccess(String xml) {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(xml));
    }

    private void mockWebClientForFailure() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenThrow(new RuntimeException("Connection refused"));
    }

    private String validXml() {
        return """
                <Regiones>
                  <Region>
                    <Numero>1</Numero>
                    <NumeroRomano>I</NumeroRomano>
                    <Nombre>Region de Test</Nombre>
                    <Comuna>
                      <Numero>10</Numero>
                    </Comuna>
                  </Region>
                  <Region>
                    <Numero>2</Numero>
                    <NumeroRomano>II</NumeroRomano>
                    <Nombre>Region Dos</Nombre>
                  </Region>
                </Regiones>
                """;
    }

    @Test
    void findAll_shouldFetchAndSaveOnFirstCall() {
        mockWebClientForSuccess(validXml());
        when(regionRepository.findById(1)).thenReturn(Optional.empty());
        when(regionRepository.findById(2)).thenReturn(Optional.empty());

        ComunaEntity comuna = new ComunaEntity();
        comuna.setId(10);
        DistritoEntity distrito = new DistritoEntity();
        distrito.setId(100);
        comuna.setDistrito(distrito);
        when(comunaRepository.findById(10)).thenReturn(Optional.of(comuna));

        RegionEntity r1 = new RegionEntity();
        r1.setId(1);
        r1.setNombre("Region de Test");
        r1.setNumRomano("I");
        RegionEntity r2 = new RegionEntity();
        r2.setId(2);
        r2.setNombre("Region Dos");
        r2.setNumRomano("II");
        when(regionRepository.findAll()).thenReturn(List.of(r1, r2));

        List<RegionEntity> result = service.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getNombre()).isEqualTo("Region de Test");
        assertThat(result.get(0).getNumRomano()).isEqualTo("I");
        assertThat(result.get(1).getNombre()).isEqualTo("Region Dos");

        verify(regionRepository, times(2)).save(any(RegionEntity.class));
        verify(distritoService).assignRegionToDistrito(eq(100), any(RegionEntity.class));
        verify(entityManager).flush();
        verify(entityManager).clear();
    }

    @Test
    void findAll_shouldReturnFromRepository_whenAlreadySaved() throws Exception {
        setSavedFlagTrue();

        RegionEntity e = new RegionEntity();
        e.setId(1);
        e.setNombre("Cached Region");
        when(regionRepository.findAll()).thenReturn(List.of(e));

        List<RegionEntity> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getNombre()).isEqualTo("Cached Region");
        verifyNoInteractions(webClient);
    }

    @Test
    void findAll_shouldReturnEmptyList_whenWebClientFails() {
        mockWebClientForFailure();
        when(regionRepository.findAll()).thenReturn(List.of());

        List<RegionEntity> result = service.findAll();

        assertThat(result).isEmpty();
        verify(regionRepository, never()).save(any());
    }

    @Test
    void findById_shouldReturnRegion_whenFound() {
        mockWebClientForSuccess(validXml());
        when(regionRepository.findById(1)).thenReturn(Optional.empty());
        when(regionRepository.findById(2)).thenReturn(Optional.empty());

        RegionEntity r = new RegionEntity();
        r.setId(1);
        r.setNombre("Region de Test");
        when(regionRepository.findById(Integer.valueOf(1))).thenReturn(Optional.of(r));

        Optional<RegionEntity> result = service.findById(1);

        assertThat(result).isPresent();
        assertThat(result.get().getNombre()).isEqualTo("Region de Test");
    }

    @Test
    void findById_shouldReturnEmpty_whenNotFound() {
        mockWebClientForSuccess(validXml());
        when(regionRepository.findById(1)).thenReturn(Optional.empty());
        when(regionRepository.findById(2)).thenReturn(Optional.empty());

        when(regionRepository.findById(Integer.valueOf(99))).thenReturn(Optional.empty());

        Optional<RegionEntity> result = service.findById(99);

        assertThat(result).isEmpty();
    }

    @Test
    void findById_shouldHandleWebClientFailure() {
        mockWebClientForFailure();
        when(regionRepository.findById(Integer.valueOf(1))).thenReturn(Optional.empty());

        Optional<RegionEntity> result = service.findById(1);

        assertThat(result).isEmpty();
    }

    private void setSavedFlagTrue() throws Exception {
        Field field = IRegionService.class.getDeclaredField("savedFlag");
        field.setAccessible(true);
        field.set(null, true);
    }
}
