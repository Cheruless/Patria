package cl.patria.diputado_profile_service.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DiputadoDTO(
        @JsonProperty("diputado_id") Integer id,
        @JsonProperty("nombreCompleto") String nombreCompleto,
        @JsonProperty("aliasPartido") String aliasPartido,
        @JsonProperty("distritoNum") Integer distrito
){}