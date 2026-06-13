package cl.patria.diputado_profile_service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
@AllArgsConstructor

public class ProfileDTO {
    private Integer id;
    private String nombreCompleto;
    private String partido;
    private Integer distrito;
    private List<ComunaDTO> comunas;
    private RegionDTO region;

    public ProfileDTO(Integer id, String nombreCompleto, String partido, Integer distrito) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.partido = partido;
        this.distrito = distrito;
    }
}
