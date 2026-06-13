package cl.patria.service;

import cl.patria.model.RegionEntity;

import java.util.List;
import java.util.Optional;

public interface RegionService {
    String uri = "https://opendata.camara.cl/camaradiputados/WServices/WSComun.asmx/retornarRegiones";
    List<RegionEntity> findAll();
    Optional<RegionEntity> findById(Integer id);
}
