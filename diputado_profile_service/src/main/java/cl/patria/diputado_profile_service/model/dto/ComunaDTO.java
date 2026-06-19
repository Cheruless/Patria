package cl.patria.diputado_profile_service.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ComunaDTO(
        @JsonProperty("id") Integer id,
        @JsonProperty("nombre") String nombre
) {
}
