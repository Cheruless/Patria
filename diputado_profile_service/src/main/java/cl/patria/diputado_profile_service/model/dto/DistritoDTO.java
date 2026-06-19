package cl.patria.diputado_profile_service.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DistritoDTO(
        @JsonProperty("id") Integer id,
        @JsonProperty("region") RegionDTO region,
        @JsonProperty("comunas") List<ComunaDTO> comunas
){}