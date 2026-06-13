package cl.patria.repository;

import cl.patria.model.DiputadoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiputadoRepository extends  JpaRepository<DiputadoEntity, Integer> {
}
