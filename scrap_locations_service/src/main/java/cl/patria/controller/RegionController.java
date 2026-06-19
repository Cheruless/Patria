package cl.patria.controller;

import cl.patria.model.RegionEntity;
import cl.patria.service.RegionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/scrap/regiones")
@Tag(name = "Regiones", description = "Endpoints para obtener información extraída de las Regiones de Chile")
public class RegionController {

    @Autowired
    private RegionService service;

    @GetMapping
    @Operation(summary = "Obtener lista completa de regiones")
    public ResponseEntity<CollectionModel<EntityModel<RegionEntity>>> returnRegiones(){
        List<RegionEntity> regiones = service.findAll();

        if (regiones == null || regiones.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        List<EntityModel<RegionEntity>> regionesModels = regiones.stream()
                .map(region -> EntityModel.of(region,
                        linkTo(methodOn(RegionController.class).returnRegion(region.getId())).withSelfRel(),
                        linkTo(methodOn(RegionController.class).returnRegiones()).withRel("regiones")))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<RegionEntity>> collectionModel = CollectionModel.of(regionesModels,
                linkTo(methodOn(RegionController.class).returnRegiones()).withSelfRel());

        return ResponseEntity.status(HttpStatus.OK).body(collectionModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una región específica por su ID")
    public ResponseEntity<EntityModel<RegionEntity>> returnRegion(@PathVariable("id") int id) {
        Optional<RegionEntity> region = service.findById(id);

        return region.map(r -> {
            EntityModel<RegionEntity> resource = EntityModel.of(r,
                    linkTo(methodOn(RegionController.class).returnRegion(id)).withSelfRel(),
                    linkTo(methodOn(RegionController.class).returnRegiones()).withRel("regiones"));
            return ResponseEntity.status(HttpStatus.OK).body(resource);
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}