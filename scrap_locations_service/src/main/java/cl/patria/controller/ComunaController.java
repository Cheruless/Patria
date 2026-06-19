package cl.patria.controller;

import cl.patria.model.ComunaEntity;
import cl.patria.service.ComunaService;
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
@RequestMapping("/api/v1/scrap/comunas")
@Tag(name = "Comunas", description = "Endpoints para obtener información extraída de las Comunas")
public class ComunaController {

    @Autowired
    private ComunaService service;

    @GetMapping
    @Operation(summary = "Obtener lista completa de comunas")
    public ResponseEntity<CollectionModel<EntityModel<ComunaEntity>>> returnComunas(){
        List<ComunaEntity> comunas = service.findAll();

        if (comunas == null || comunas.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        List<EntityModel<ComunaEntity>> comunasModels = comunas.stream()
                .map(comuna -> EntityModel.of(comuna,
                        linkTo(methodOn(ComunaController.class).returnComuna(comuna.getId())).withSelfRel(),
                        linkTo(methodOn(ComunaController.class).returnComunas()).withRel("comunas")))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<ComunaEntity>> collectionModel = CollectionModel.of(comunasModels,
                linkTo(methodOn(ComunaController.class).returnComunas()).withSelfRel());

        return ResponseEntity.status(HttpStatus.OK).body(collectionModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una comuna específica por su ID")
    public ResponseEntity<EntityModel<ComunaEntity>> returnComuna(@PathVariable("id") int id){
        Optional<ComunaEntity> comuna = service.findById(id);

        return comuna.map(c -> {
            EntityModel<ComunaEntity> resource = EntityModel.of(c,
                    linkTo(methodOn(ComunaController.class).returnComuna(id)).withSelfRel(),
                    linkTo(methodOn(ComunaController.class).returnComunas()).withRel("comunas"));
            return ResponseEntity.status(HttpStatus.OK).body(resource);
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}