package cl.patria.diputado_profile_service.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DiputadoDTO(
        @JsonAlias({"diputado_id", "diputadoId", "id"})
        @JsonProperty("diputado_id") Integer id,

        @JsonAlias({"nombreCompleto", "nombre_completo"})
        @JsonProperty("nombreCompleto") String nombreCompleto,

        @JsonAlias({"aliasPartido", "alias_partido"})
        @JsonProperty("aliasPartido") String aliasPartido,

        @JsonAlias({"distritoNum", "distrito_num"})
        @JsonProperty("distritoNum") Integer distrito
){}