package cl.patria.diputado_profile_service.service;

import cl.patria.diputado_profile_service.model.ProfileEntity;
import cl.patria.diputado_profile_service.model.dto.ProfileDTO;

import java.util.List;
import java.util.Optional;


public interface ProfileService {
    String uriDiputadosScraper = "http://localhost:8081/api/v1/scrap/diputados";
    String uriDistritosScraper = "http://localhost:8082/api/v1/scrap/distritos";
    List<ProfileEntity> getProfiles();
    Optional<ProfileEntity> getProfile(int id);

}
