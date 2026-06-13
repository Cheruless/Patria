package cl.patria.repository;

import cl.patria.model.ComunaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComunaRepository extends JpaRepository<ComunaEntity, Integer> {
}
