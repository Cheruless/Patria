package cl.patria.service;

import cl.patria.model.ComunaEntity;

import java.util.List;
import java.util.Optional;

public interface ComunaService {
    String uri = "https://opendata.camara.cl/camaradiputados/WServices/WSComun.asmx/retornarComunas";
    List<ComunaEntity> findAll();
    Optional<ComunaEntity> findById(Integer id);
}
