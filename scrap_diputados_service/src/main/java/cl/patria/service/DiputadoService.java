package cl.patria.service;

import cl.patria.model.DiputadoEntity;

import java.util.List;
import java.util.Optional;

public interface DiputadoService {
    List<DiputadoEntity> getDiputados();
    Optional<DiputadoEntity> getDiputado(int id);
}