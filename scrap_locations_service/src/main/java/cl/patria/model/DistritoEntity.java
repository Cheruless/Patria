package cl.patria.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@Entity
@Table(name = "Distrito")
@Getter
@Setter
@ToString(exclude = {"comunas", "region"})
public class DistritoEntity {
    @Id
    private int id;

    @OneToMany(mappedBy = "distrito", fetch = FetchType.EAGER)
    @JsonIgnoreProperties("distrito")
    Set<ComunaEntity> comunas;

    @ManyToOne
    @JoinColumn(name = "region_id", nullable = true)
    @JsonIgnoreProperties("distritos")
    private RegionEntity region;
}