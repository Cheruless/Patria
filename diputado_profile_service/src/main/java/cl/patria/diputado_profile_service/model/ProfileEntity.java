package cl.patria.diputado_profile_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Entity
@Table(name = "Profile")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProfileEntity {
    @Id
    private Integer id;

    @Column(name = "nombre_completo") private String nombreCompleto;
    @Column(name = "partido") private String partido;
    @Column(name = "distrito_id") private int distritoId;
    @Column(name = "region")  private String region;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "profile_comunas", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "comuna")
    private List<String> comunas;
}