package cl.patria.diputado_profile_service.controller;

import cl.patria.diputado_profile_service.model.ProfileEntity;
import cl.patria.diputado_profile_service.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/profiles")
@Tag(name = "Perfiles de Diputados", description = "Endpoints para obtener los perfiles consolidados de Diputados con su informacion territorial")
public class ProfileController {

    @Autowired
    private ProfileService service;

    @GetMapping
    @Operation(summary = "Obtener lista completa de perfiles de diputados")
    public ResponseEntity<CollectionModel<EntityModel<ProfileEntity>>> returnProfiles() {
        List<ProfileEntity> profiles = service.getProfiles();

        if (profiles == null || profiles.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<EntityModel<ProfileEntity>> profilesModels = profiles.stream()
                .map(profile -> EntityModel.of(profile,
                        linkTo(methodOn(ProfileController.class).returnProfileById(profile.getId())).withSelfRel(),
                        linkTo(methodOn(ProfileController.class).returnProfiles()).withRel("profiles")))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<ProfileEntity>> collectionModel = CollectionModel.of(profilesModels,
                linkTo(methodOn(ProfileController.class).returnProfiles()).withSelfRel());

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un perfil especifico de un Diputado por su identificador")
    public ResponseEntity<EntityModel<ProfileEntity>> returnProfileById(@PathVariable("id") int id) {
        return service.getProfile(id)
                .map(p -> ResponseEntity.ok(
                        EntityModel.of(p,
                                linkTo(methodOn(ProfileController.class).returnProfileById(id)).withSelfRel(),
                                linkTo(methodOn(ProfileController.class).returnProfiles()).withRel("profiles"))))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}