package cl.patria.controller;

import cl.patria.model.DiputadoEntity;
import cl.patria.service.DiputadoService;
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
@RequestMapping("/api/v1/scrap/diputados")
@Tag(name = "Diputados", description = "Endpoints para obtener información extraída de los Diputados")
public class DiputadoController {

    @Autowired
    private DiputadoService service;

    @GetMapping
    @Operation(summary = "Obtener lista completa de diputados")
    public ResponseEntity<CollectionModel<EntityModel<DiputadoEntity>>> returnDiputados() {
        List<DiputadoEntity> diputados = service.getDiputados();

        if (diputados == null || diputados.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        List<EntityModel<DiputadoEntity>> diputadosModels = diputados.stream()
                .map(diputado -> EntityModel.of(diputado,
                        linkTo(methodOn(DiputadoController.class).returnDiputadoById(diputado.getDiputado_id())).withSelfRel(),
                        linkTo(methodOn(DiputadoController.class).returnDiputados()).withRel("diputados")))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<DiputadoEntity>> collectionModel = CollectionModel.of(diputadosModels,
                linkTo(methodOn(DiputadoController.class).returnDiputados()).withSelfRel());

        return ResponseEntity.status(HttpStatus.OK).body(collectionModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un diputado específico por su ID")
    public ResponseEntity<EntityModel<DiputadoEntity>> returnDiputadoById(@PathVariable("id") int id) {
        Optional<DiputadoEntity> diputado = service.getDiputado(id);

        return diputado.map(d -> {
            EntityModel<DiputadoEntity> resource = EntityModel.of(d,
                    linkTo(methodOn(DiputadoController.class).returnDiputadoById(id)).withSelfRel(),
                    linkTo(methodOn(DiputadoController.class).returnDiputados()).withRel("diputados"));
            return ResponseEntity.status(HttpStatus.OK).body(resource);
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}