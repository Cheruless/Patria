package cl.patria.service;

import cl.patria.model.DistritoEntity;
import cl.patria.model.RegionEntity;

import java.util.List;
import java.util.Optional;

public interface DistritoService {
    String uri = "https://opendata.camara.cl/camaradiputados/WServices/WSComun.asmx/retornarDistritos";
    List<DistritoEntity> findAll();
    Optional<DistritoEntity> findById(Integer id);
    void assignRegionToDistrito(int distritoId, RegionEntity region);
}
