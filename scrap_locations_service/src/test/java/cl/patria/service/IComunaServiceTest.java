package cl.patria.service;

import cl.patria.model.ComunaEntity;
import cl.patria.repository.ComunaRepository;
import cl.patria.service.impl.IComunaService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IComunaServiceTest {

    @Mock
    private ComunaRepository repo;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private IComunaService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new IComunaService(webClient);
        ReflectionTestUtils.setField(service, "repository", repo);
        resetStaticFlag();
    }

    private void resetStaticFlag() throws Exception {
        Field field = IComunaService.class.getDeclaredField("savedFlag");
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
                <Comunas>
                  <Comuna>
                    <Numero>1</Numero>
                    <Nombre>Comuna Uno</Nombre>
                  </Comuna>
                  <Comuna>
                    <Numero>2</Numero>
                    <Nombre>Comuna Dos</Nombre>
                  </Comuna>
                </Comunas>
                """;
    }

    @Test
    void findAll_shouldFetchAndSaveOnFirstCall() {
        mockWebClientForSuccess(validXml());
        when(repo.findById(1)).thenReturn(Optional.empty());
        when(repo.findById(2)).thenReturn(Optional.empty());

        ComunaEntity e1 = new ComunaEntity();
        e1.setId(1);
        e1.setNombre("Comuna Uno");
        ComunaEntity e2 = new ComunaEntity();
        e2.setId(2);
        e2.setNombre("Comuna Dos");
        when(repo.findAll()).thenReturn(List.of(e1, e2));

        List<ComunaEntity> result = service.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getNombre()).isEqualTo("Comuna Uno");
        assertThat(result.get(1).getNombre()).isEqualTo("Comuna Dos");
        verify(repo).saveAll(anyList());
        verify(repo).flush();
    }

    @Test
    void findAll_shouldReturnFromRepository_whenAlreadySaved() throws Exception {
        setSavedFlagTrue();

        ComunaEntity e = new ComunaEntity();
        e.setId(1);
        e.setNombre("Cached Comuna");
        when(repo.findAll()).thenReturn(List.of(e));

        List<ComunaEntity> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getNombre()).isEqualTo("Cached Comuna");
        verifyNoInteractions(webClient);
    }

    @Test
    void findAll_shouldReturnEmptyList_whenWebClientFails() {
        mockWebClientForFailure();
        when(repo.findAll()).thenReturn(List.of());

        List<ComunaEntity> result = service.findAll();

        assertThat(result).isEmpty();
        verify(repo, never()).saveAll(any());
    }

    @Test
    void findById_shouldReturnComuna_whenFound() {
        mockWebClientForSuccess(validXml());
        when(repo.findById(1)).thenReturn(Optional.empty());
        when(repo.findById(2)).thenReturn(Optional.empty());

        ComunaEntity e = new ComunaEntity();
        e.setId(1);
        e.setNombre("Comuna Uno");
        when(repo.findById(Integer.valueOf(1))).thenReturn(Optional.of(e));

        Optional<ComunaEntity> result = service.findById(1);

        assertThat(result).isPresent();
        assertThat(result.get().getNombre()).isEqualTo("Comuna Uno");
    }

    @Test
    void findById_shouldReturnEmpty_whenNotFound() {
        mockWebClientForSuccess(validXml());
        when(repo.findById(1)).thenReturn(Optional.empty());
        when(repo.findById(2)).thenReturn(Optional.empty());
        when(repo.findById(Integer.valueOf(99))).thenReturn(Optional.empty());

        Optional<ComunaEntity> result = service.findById(99);

        assertThat(result).isEmpty();
    }

    @Test
    void findById_shouldHandleWebClientFailure() {
        mockWebClientForFailure();
        when(repo.findById(Integer.valueOf(1))).thenReturn(Optional.empty());

        Optional<ComunaEntity> result = service.findById(1);

        assertThat(result).isEmpty();
    }

    private void setSavedFlagTrue() throws Exception {
        Field field = IComunaService.class.getDeclaredField("savedFlag");
        field.setAccessible(true);
        field.set(null, true);
    }
}
