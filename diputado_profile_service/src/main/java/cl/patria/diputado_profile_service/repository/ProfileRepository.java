package cl.patria.diputado_profile_service.repository;

import cl.patria.diputado_profile_service.model.ProfileEntity;
import cl.patria.diputado_profile_service.model.dto.ProfileDTO;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends JpaRepository<ProfileEntity, Integer> {
}