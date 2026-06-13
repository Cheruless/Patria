package cl.patria.service.impl;

import cl.patria.model.DistritoEntity;
import cl.patria.model.RegionEntity;
import cl.patria.repository.DistritoRepository;
import cl.patria.service.DistritoService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class IDistritoService implements DistritoService {

    @Autowired
    private DistritoRepository repository;

    @Override
    public List<DistritoEntity> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<DistritoEntity> findById(Integer id) {
        return Optional.empty();
    }

    @Override
    public void assignRegionToDistrito(int distritoId, RegionEntity region) {
        repository.findById(distritoId).ifPresent(distrito -> {
            distrito.setRegion(region);
            repository.save(distrito);
        });
    }
}
