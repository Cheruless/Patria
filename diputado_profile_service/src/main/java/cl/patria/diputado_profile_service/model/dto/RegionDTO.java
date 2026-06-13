package cl.patria.diputado_profile_service.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RegionDTO(
        @JsonProperty("id") Integer id,
        @JsonProperty("nombre") String nombre,
        @JsonProperty("numRomano") String numRomano
){}