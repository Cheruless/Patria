package cl.patria.diputado_profile_service.controller;

import cl.patria.diputado_profile_service.model.ProfileEntity;
import cl.patria.diputado_profile_service.model.dto.ProfileDTO;
import cl.patria.diputado_profile_service.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/profiles")
public class ProfileController {

    @Autowired
    private ProfileService service;

    @GetMapping
    public ResponseEntity<List<ProfileEntity>> returnProfiles() {
        List<ProfileEntity> profiles = service.getProfiles();

        if (profiles == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        return (!profiles.isEmpty())
                ? ResponseEntity.status(HttpStatus.OK).body(profiles)
                : ResponseEntity.status(HttpStatus.NO_CONTENT).body(profiles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfileEntity> returnProfileById(@PathVariable("id") int id) {
        Optional<ProfileEntity> profile = service.getProfile(id);

        return (profile.isPresent())
                ? ResponseEntity.status(HttpStatus.OK).body(profile.get())
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
}

