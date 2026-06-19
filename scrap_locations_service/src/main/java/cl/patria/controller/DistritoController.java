package cl.patria.controller;

import cl.patria.model.DistritoEntity;
import cl.patria.service.DistritoService;
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
@RequestMapping("/api/v1/scrap/distritos")
@Tag(name = "Distritos", description = "Endpoints para obtener información extraída de los Distritos Electorales")
public class DistritoController {

    @Autowired
    private DistritoService service;

    @GetMapping
    @Operation(summary = "Obtener lista completa de distritos")
    public ResponseEntity<CollectionModel<EntityModel<DistritoEntity>>> returnDistritos() {
        List<DistritoEntity> distritos = service.findAll();

        if (distritos == null || distritos.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        List<EntityModel<DistritoEntity>> distritosModels = distritos.stream()
                .map(distrito -> EntityModel.of(distrito,
                        linkTo(methodOn(DistritoController.class).returnDistritoById(distrito.getId())).withSelfRel(),
                        linkTo(methodOn(DistritoController.class).returnDistritos()).withRel("distritos")))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<DistritoEntity>> collectionModel = CollectionModel.of(distritosModels,
                linkTo(methodOn(DistritoController.class).returnDistritos()).withSelfRel());

        return ResponseEntity.status(HttpStatus.OK).body(collectionModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un distrito específico por su ID")
    public ResponseEntity<EntityModel<DistritoEntity>> returnDistritoById(@PathVariable("id") int id){
        Optional<DistritoEntity> distrito = service.findById(id);

        return distrito.map(d -> {
            EntityModel<DistritoEntity> resource = EntityModel.of(d,
                    linkTo(methodOn(DistritoController.class).returnDistritoById(id)).withSelfRel(),
                    linkTo(methodOn(DistritoController.class).returnDistritos()).withRel("distritos"));
            return ResponseEntity.status(HttpStatus.OK).body(resource);
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}