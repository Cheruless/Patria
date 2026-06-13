package cl.patria.diputado_profile_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Profile")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProfileEntity {
    @Id
    private int id;
    @Column(name = "nombre_completo") private String nombreCompleto;
    @Column(name = "distrito_id") private int distritoId;
}