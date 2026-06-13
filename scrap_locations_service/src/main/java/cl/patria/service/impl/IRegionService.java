package cl.patria.service.impl;

import cl.patria.model.RegionEntity;
import cl.patria.repository.ComunaRepository;
import cl.patria.repository.RegionRepository;
import cl.patria.service.DistritoService;
import cl.patria.service.RegionService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
@Transactional
public class IRegionService implements RegionService {

    private static boolean savedFlag = false;

    @Autowired
    private RegionRepository regionRepository;
    @Autowired
    private ComunaRepository comunaRepository;
    @Autowired
    private DistritoService distritoService;
    @PersistenceContext
    private EntityManager entityManager; // Inyectar el gestor de persistencia

    private final WebClient webClient;

    public IRegionService(WebClient webClient) {
        this.webClient = webClient;
    }

    /*++++++++++++++++MÉTODOS PÚBLICOS DE SERVICE++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

    @Override
    public List<RegionEntity> findAll() {
        if (!savedFlag)
            processRegionesData();
        return regionRepository.findAll();
    }

    @Override
    public Optional<RegionEntity> findById(Integer id) {
        if (!savedFlag)
            processRegionesData();
        return regionRepository.findById(Integer.valueOf(id));
    }

    /*++++++++++++++++MÉTODOS PRIVADOS DE SERVICE++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

    @EventListener(ApplicationReadyEvent.class)
    protected void initDataOnStartup() {
        if (!savedFlag)
            processRegionesData();
    }

    /* Method orquestador: Obtiene XML, parsea, guarda en BD y enlaza distritos. */
    private void processRegionesData() {
        String xmlContent = fetchXmlFromApi();
        if (xmlContent == null) {
            return;
        }

        Document doc = Jsoup.parse(xmlContent, "", Parser.xmlParser());

        for (Element regionElement : doc.selectXpath("//Region")) {
            RegionEntity region = extractRegionFromXml(regionElement);

            if (regionRepository.findById(region.getId()).isEmpty()) {
                regionRepository.save(region);
            }

            assignDistritosToRegion(region, regionElement);
        }

        savedFlag = true;
        // Obliga a Hibernate a sincronizar con la base de datos
        entityManager.flush();
        entityManager.clear();
    }

    /* Solo se encarga de la petición HTTP */
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
            System.err.println("|scrap_locations_service|Aviso: No se pudo obtener el XML | Motivo: " + e.getMessage());
            return null;
        }
    }

    /* Solo se encarga de mapear el nodo XML a la entidad Java */
    private RegionEntity extractRegionFromXml(Element regionElement) {
        RegionEntity region = new RegionEntity();

        String idElement = regionElement.selectXpath("./Numero").first().text();
        region.setId(Integer.parseInt(idElement));

        String numRomanoElement = regionElement.selectXpath("./NumeroRomano").text();
        region.setNumRomano(numRomanoElement);

        String nombreElement = regionElement.selectXpath("./Nombre").first().text();
        region.setNombre(nombreElement);

        return region;
    }

    /* Maneja la lógica de enlace con Comunas y Distritos */
    private void assignDistritosToRegion(RegionEntity region, Element element) {
        Set<Integer> distritosProcesados = new HashSet<>();

        for (Element comunaElement : element.selectXpath(".//Comuna")) {
            String idComunaStr = comunaElement.selectXpath("./Numero").text();
            int idComuna = Integer.parseInt(idComunaStr);

            comunaRepository.findById(idComuna).ifPresent(comunaBD -> {
                if (comunaBD.getDistrito() != null) {
                    int distritoId = comunaBD.getDistrito().getId();

                    if (!distritosProcesados.contains(distritoId)) {
                        distritoService.assignRegionToDistrito(distritoId, region);
                        distritosProcesados.add(distritoId);
                    }
                }
            });
        }
    }
}