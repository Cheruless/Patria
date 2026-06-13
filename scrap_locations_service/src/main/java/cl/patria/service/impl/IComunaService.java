package cl.patria.service.impl;

import cl.patria.model.ComunaEntity;
import cl.patria.repository.ComunaRepository;
import cl.patria.service.ComunaService;
import cl.patria.util.config.WebClientConfig;
import jakarta.transaction.Transactional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class IComunaService implements ComunaService {

    @Autowired
    private ComunaRepository repository;
    private final WebClient webClient;

    public IComunaService(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public List<ComunaEntity> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<ComunaEntity> findById(Integer id) {
        return Optional.empty();
    }

    private String fetchXmlFromApi() {
        try {
            return webClient.get()
                    .uri(uri)
                    .header(HttpHeaders.USER_AGENT, "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36")
                    .header(HttpHeaders.ACCEPT, "application/xml")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            System.err.println("|scrap_locations_service|Aviso: No se pudo obtener el XML desde Cámara de Diputados | Motivo: " + e.getMessage());
            return null;
        }
    }

    private List<ComunaEntity> parseXmlToComunas(String xmlContent) {
        List<ComunaEntity> comunas = new ArrayList<>();
        Document doc = Jsoup.parse(xmlContent, "", Parser.xmlParser());

        for (Element e : doc.selectXpath("//Comuna")) {
            ComunaEntity comunaBuffer = new ComunaEntity();

            // Id de Comuna
            String idElement = e.selectXpath("./Numero").text();
            comunaBuffer.setId(Integer.parseInt(idElement));

            String nombre = e.selectXpath("./Nombre").text();
            comunaBuffer.setNombre(nombre);

            comunas.add(comunaBuffer);
        }

        return comunas;
    }
}
