package cl.patria.service.impl;

import cl.patria.model.DiputadoEntity;
import cl.patria.repository.DiputadoRepository;
import cl.patria.service.DiputadoService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class IDiputadoService implements DiputadoService {

    private static boolean savedFlag = false;

    @Autowired
    private DiputadoRepository repo;
    @Autowired
    private DataSource dataSource;
    @PersistenceContext
    private EntityManager entityManager;

    private final WebClient webClient;

    public IDiputadoService(WebClient webClient) {
        this.webClient = webClient;
    }

    /*++++++++++++++++MÉTODOS PÚBLICOS DE SERVICE++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

    @Override
    public List<DiputadoEntity> getDiputados() {
        if (!savedFlag)
            processDiputadosData();
        return repo.findAll();
    }

    @Override
    public Optional<DiputadoEntity> getDiputado(int id) {
        if (!savedFlag)
            processDiputadosData();
        return repo.findById(Integer.valueOf(id));
    }

    /*++++++++++++++++MÉTODOS PRIVADOS DE SERVICE++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

    @EventListener(ApplicationReadyEvent.class)
    protected void initDataOnStartup() {
        if (!savedFlag)
            processDiputadosData();
    }

    /* Method 'padre' que orquesta la obtención, parseo, guardado y scripts. */
    private void processDiputadosData() {
        String xmlContent = fetchXmlFromApi();

        if (xmlContent == null) {
            return;
        }

        List<DiputadoEntity> diputados = parseXmlToDiputados(xmlContent);

        saveOrUpdateDiputados(diputados);
        ejecutarSqlDistritos();

        entityManager.clear();
        savedFlag = true;
    }

    /* Maneja exclusivamente la petición HTTP y captura excepciones de conexión. */
    private String fetchXmlFromApi() {
        String urlDiputadosXML = "https://opendata.camara.cl/camaradiputados/WServices/WSDiputado.asmx/retornarDiputadosPeriodoActual";

        try {
            return webClient.get()
                    .uri(urlDiputadosXML)
                    .header(HttpHeaders.USER_AGENT, "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36")
                    .header(HttpHeaders.ACCEPT, "application/xml")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            System.err.println("|scrap_diputados_service|Aviso: No se pudo obtener el XML desde Cámara de Diputados | Motivo: " + e.getMessage());
            return null;
        }
    }

    /* Maneja exclusivamente la lógica de web scraping (Jsoup) y mapeo a DTO. */
    private List<DiputadoEntity> parseXmlToDiputados(String xmlContent) {
        List<DiputadoEntity> diputados = new ArrayList<>();
        Document doc = Jsoup.parse(xmlContent, "", Parser.xmlParser());

        for (Element e : doc.selectXpath("//DiputadoPeriodo")) {
            Element idElement = e.selectXpath(".//Diputado/Id").first();

            if (idElement != null && !idElement.text().isEmpty()) {
                DiputadoEntity diputadoBuffer = new DiputadoEntity();
                diputadoBuffer.setDiputado_id(Integer.parseInt(idElement.text()));

                String n1 = e.selectXpath(".//Diputado/Nombre").text();
                String n2 = e.selectXpath(".//Diputado/Nombre2").text();
                String ap = e.selectXpath(".//Diputado/ApellidoPaterno").text();
                String am = e.selectXpath(".//Diputado/ApellidoMaterno").text();

                String completo = String.format("%s %s %s %s", n1, n2, ap, am)
                        .replaceAll("\\s+", " ")
                        .trim();

                diputadoBuffer.setNombreCompleto(completo.isEmpty() ? null : completo);

                Element partidoAlias = e.selectXpath(".//Militancias/Militancia[last()]/Partido/Alias").first();
                if (partidoAlias != null) {
                    diputadoBuffer.setAliasPartido(partidoAlias.text());
                }

                diputados.add(diputadoBuffer);
            }
        }
        return diputados;
    }

    /* Maneja exclusivamente las validaciones de persistencia e inserción a BDD. */
    private void saveOrUpdateDiputados(List<DiputadoEntity> diputados) {
        for (DiputadoEntity d : diputados) {
            Optional<DiputadoEntity> diputadoExistente = repo.findById(d.getDiputado_id());

            if (diputadoExistente.isPresent()) {
                DiputadoEntity diputado = diputadoExistente.get();
                if (d.getDistritoNum() != null) {
                    diputado.setDistritoNum(d.getDistritoNum());
                }
                diputado.setNombreCompleto(d.getNombreCompleto());
                diputado.setAliasPartido(d.getAliasPartido());
                repo.save(diputado);
            } else {
                repo.save(d);
            }
        }
        repo.flush();
    }

    private void ejecutarSqlDistritos() {
        System.out.println("Ejecutando script de actualización de distritos...");
        Connection connection = DataSourceUtils.getConnection(dataSource);

        try {
            ClassPathResource resource = new ClassPathResource("scripts/actualizar_distritos.sql");
            ScriptUtils.executeSqlScript(connection, resource);
            System.out.println("Distritos actualizados correctamente en la BD.");
        } catch (Exception e) {
            System.err.println("Error ejecutando el script de distritos: " + e.getMessage());
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}