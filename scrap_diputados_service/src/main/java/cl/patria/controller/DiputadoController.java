package cl.patria.controller;

import cl.patria.model.DiputadoEntity;
import cl.patria.service.DiputadoService;
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
@RequestMapping("/api/v1/scrap/diputados")
public class DiputadoController {
    @Autowired
    private DiputadoService service;

    @GetMapping
    public ResponseEntity<List<DiputadoEntity>> returnDiputados() {
        List<DiputadoEntity> diputados = service.getDiputados();

        if (diputados == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        return (!diputados.isEmpty())
                ? ResponseEntity.status(HttpStatus.OK).body(diputados)
                : ResponseEntity.status(HttpStatus.NO_CONTENT).body(diputados);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<DiputadoEntity>> returnDiputadoById(@PathVariable("id") int id) {
        Optional<DiputadoEntity> diputado = service.getDiputado(id);

        return (diputado.isPresent())
                ? ResponseEntity.status(HttpStatus.OK).body(diputado)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
}
