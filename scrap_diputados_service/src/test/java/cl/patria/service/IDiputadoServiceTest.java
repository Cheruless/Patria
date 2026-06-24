package cl.patria.service;

import cl.patria.model.DiputadoEntity;
import cl.patria.repository.DiputadoRepository;
import cl.patria.service.impl.IDiputadoService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IDiputadoServiceTest {

    @Mock
    private DiputadoRepository repo;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private DataSource dataSource;

    @Mock
    private EntityManager entityManager;

    @Mock
    private Connection connection;

    private IDiputadoService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new IDiputadoService(webClient);
        ReflectionTestUtils.setField(service, "repo", repo);
        ReflectionTestUtils.setField(service, "dataSource", dataSource);
        ReflectionTestUtils.setField(service, "entityManager", entityManager);
        resetStaticFlag();
    }

    private void resetStaticFlag() throws Exception {
        Field field = IDiputadoService.class.getDeclaredField("savedFlag");
        field.setAccessible(true);
        field.set(null, false);
    }

    private void mockFullWebClientChain() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    private void mockWebClientForSuccess(String xml) {
        mockFullWebClientChain();
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(xml));
        try {
            when(dataSource.getConnection()).thenReturn(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void mockWebClientForFailure() {
        mockFullWebClientChain();
        when(requestHeadersSpec.retrieve()).thenThrow(new RuntimeException("Connection refused"));
    }

    private String validXml() {
        return """
                <Diputados>
                  <DiputadoPeriodo>
                    <Diputado>
                      <Id>1</Id>
                      <Nombre>NOMBRE1</Nombre>
                      <Nombre2>SEGUNDO</Nombre2>
                      <ApellidoPaterno>APELLIDO_P</ApellidoPaterno>
                      <ApellidoMaterno>APELLIDO_M</ApellidoMaterno>
                    </Diputado>
                    <Militancias>
                      <Militancia>
                        <Partido>
                          <Alias>PARTIDO_TEST</Alias>
                        </Partido>
                      </Militancia>
                    </Militancias>
                  </DiputadoPeriodo>
                  <DiputadoPeriodo>
                    <Diputado>
                      <Id>2</Id>
                      <Nombre>NOMBRE2</Nombre>
                      <Nombre2></Nombre2>
                      <ApellidoPaterno>AP2</ApellidoPaterno>
                      <ApellidoMaterno>AM2</ApellidoMaterno>
                    </Diputado>
                    <Militancias>
                      <Militancia>
                        <Partido>
                          <Alias>PARTIDO2</Alias>
                        </Partido>
                      </Militancia>
                    </Militancias>
                  </DiputadoPeriodo>
                </Diputados>
                """;
    }

    @Test
    void getDiputados_shouldFetchAndSaveOnFirstCall() {
        mockWebClientForSuccess(validXml());
        when(repo.findById(1)).thenReturn(Optional.empty());
        when(repo.findById(2)).thenReturn(Optional.empty());

        List<DiputadoEntity> expected = List.of(
                createEntity(1, "NOMBRE1 SEGUNDO APELLIDO_P APELLIDO_M", "PARTIDO_TEST"),
                createEntity(2, "NOMBRE2 AP2 AM2", "PARTIDO2")
        );
        when(repo.findAll()).thenReturn(expected);

        List<DiputadoEntity> result = service.getDiputados();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getNombreCompleto()).isEqualTo("NOMBRE1 SEGUNDO APELLIDO_P APELLIDO_M");
        assertThat(result.get(0).getAliasPartido()).isEqualTo("PARTIDO_TEST");
        assertThat(result.get(1).getNombreCompleto()).isEqualTo("NOMBRE2 AP2 AM2");

        verify(repo, times(2)).save(any(DiputadoEntity.class));
        verify(repo).flush();
        verify(entityManager).clear();
    }

    @Test
    void getDiputados_shouldReturnFromRepository_whenAlreadySaved() throws Exception {
        setSavedFlagTrue();

        DiputadoEntity entity = createEntity(1, "Cached", "Partido");
        when(repo.findAll()).thenReturn(List.of(entity));

        List<DiputadoEntity> result = service.getDiputados();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getNombreCompleto()).isEqualTo("Cached");

        verify(repo).findAll();
        verifyNoInteractions(webClient);
    }

    @Test
    void getDiputados_shouldReturnEmptyList_whenWebClientFails() {
        mockWebClientForFailure();
        when(repo.findAll()).thenReturn(List.of());

        List<DiputadoEntity> result = service.getDiputados();

        assertThat(result).isEmpty();
        verify(repo, never()).save(any());
    }

    @Test
    void getDiputados_shouldHandleInvalidXmlGracefully() {
        mockWebClientForSuccess("<invalid>xml");
        when(repo.findAll()).thenReturn(List.of());

        List<DiputadoEntity> result = service.getDiputados();

        assertThat(result).isEmpty();
        verify(repo, never()).save(any());
    }

    @Test
    void getDiputado_shouldReturnDiputado_whenFound() {
        mockWebClientForSuccess(validXml());
        when(repo.findById(1)).thenReturn(Optional.empty());
        when(repo.findById(2)).thenReturn(Optional.empty());

        DiputadoEntity entity = createEntity(1, "NOMBRE1 SEGUNDO APELLIDO_P APELLIDO_M", "PARTIDO_TEST");
        when(repo.findById(Integer.valueOf(1))).thenReturn(Optional.of(entity));

        Optional<DiputadoEntity> result = service.getDiputado(1);

        assertThat(result).isPresent();
        assertThat(result.get().getNombreCompleto()).isEqualTo("NOMBRE1 SEGUNDO APELLIDO_P APELLIDO_M");
        verify(repo, times(2)).save(any(DiputadoEntity.class));
    }

    @Test
    void getDiputado_shouldReturnEmpty_whenNotFound() {
        mockWebClientForSuccess(validXml());
        when(repo.findById(1)).thenReturn(Optional.empty());
        when(repo.findById(2)).thenReturn(Optional.empty());
        when(repo.findById(Integer.valueOf(99))).thenReturn(Optional.empty());

        Optional<DiputadoEntity> result = service.getDiputado(99);

        assertThat(result).isEmpty();
    }

    @Test
    void getDiputado_shouldHandleWebClientFailure() {
        mockWebClientForFailure();

        Optional<DiputadoEntity> result = service.getDiputado(1);

        assertThat(result).isEmpty();
    }

    private void setSavedFlagTrue() throws Exception {
        Field field = IDiputadoService.class.getDeclaredField("savedFlag");
        field.setAccessible(true);
        field.set(null, true);
    }

    private DiputadoEntity createEntity(int id, String nombre, String partido) {
        DiputadoEntity e = new DiputadoEntity();
        e.setDiputado_id(id);
        e.setNombreCompleto(nombre);
        e.setAliasPartido(partido);
        return e;
    }
}
