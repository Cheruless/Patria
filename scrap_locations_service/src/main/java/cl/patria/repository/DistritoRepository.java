package cl.patria.repository;

import cl.patria.model.DistritoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DistritoRepository extends JpaRepository<DistritoEntity, Integer> {
}
