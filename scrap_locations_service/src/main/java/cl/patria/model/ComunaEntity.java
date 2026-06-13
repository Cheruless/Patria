package cl.patria.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "Comuna")
@Getter
@Setter
@ToString(exclude = "distrito")
public class ComunaEntity {
    @Id
    private int id;

    @Column(name = "nombre") private String nombre;

    @ManyToOne
    @JoinColumn(name = "distrito_id", nullable=true)
    @JsonIgnoreProperties({"comunas", "region"})
    private DistritoEntity distrito;
}
