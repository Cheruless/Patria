package cl.patria.service;

import cl.patria.model.DistritoEntity;
import cl.patria.model.RegionEntity;
import cl.patria.repository.DistritoRepository;
import cl.patria.service.impl.IDistritoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IDistritoServiceTest {

    @Mock
    private DistritoRepository repo;

    private IDistritoService service;

    @BeforeEach
    void setUp() {
        service = new IDistritoService();
        ReflectionTestUtils.setField(service, "repository", repo);
    }

    @Test
    void findAll_shouldReturnAllDistritos() {
        DistritoEntity d1 = new DistritoEntity();
        d1.setId(10);
        DistritoEntity d2 = new DistritoEntity();
        d2.setId(20);
        when(repo.findAll()).thenReturn(List.of(d1, d2));

        List<DistritoEntity> result = service.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(10);
        assertThat(result.get(1).getId()).isEqualTo(20);
    }

    @Test
    void findById_shouldAlwaysReturnEmpty() {
        Optional<DistritoEntity> result = service.findById(10);

        assertThat(result).isEmpty();
        verifyNoInteractions(repo);
    }

    @Test
    void assignRegionToDistrito_shouldSetRegion_whenDistritoFound() {
        DistritoEntity distrito = new DistritoEntity();
        distrito.setId(10);
        RegionEntity region = new RegionEntity();
        region.setId(1);
        when(repo.findById(10)).thenReturn(Optional.of(distrito));

        service.assignRegionToDistrito(10, region);

        assertThat(distrito.getRegion()).isEqualTo(region);
        verify(repo).save(distrito);
    }

    @Test
    void assignRegionToDistrito_shouldDoNothing_whenDistritoNotFound() {
        when(repo.findById(99)).thenReturn(Optional.empty());

        service.assignRegionToDistrito(99, new RegionEntity());

        verify(repo, never()).save(any());
    }
}
