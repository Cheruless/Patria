package cl.patria.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@Entity
@Table(name = "Region")
@Getter
@Setter
@ToString(exclude = "distritos")
public class RegionEntity {
    @Id
    private int id;
    @Column(name = "nombre") private String nombre;
    @Column(name = "numero") private String numRomano;

    @OneToMany(mappedBy = "region", fetch = FetchType.EAGER)
    Set<DistritoEntity> distritos;
}
