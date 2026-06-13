package cl.patria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "Diputado")
@Data
public class DiputadoEntity {
    @Id
    private int diputado_id;
    @Column(name = "nombre_completo")private String nombreCompleto;
    @Column(name = "alias_partido") String aliasPartido;
    @Column(name = "distrito_num") Integer distritoNum;
}
