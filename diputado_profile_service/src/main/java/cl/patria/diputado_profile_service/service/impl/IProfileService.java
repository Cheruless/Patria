package cl.patria.diputado_profile_service.service.impl;

import cl.patria.diputado_profile_service.model.ProfileEntity;
import cl.patria.diputado_profile_service.model.dto.ComunaDTO;
import cl.patria.diputado_profile_service.model.dto.DiputadoDTO;
import cl.patria.diputado_profile_service.model.dto.DistritoDTO;
import cl.patria.diputado_profile_service.model.dto.ProfileDTO;
import cl.patria.diputado_profile_service.repository.ProfileRepository;
import cl.patria.diputado_profile_service.service.ProfileService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class IProfileService implements ProfileService {

    private static boolean savedFlag = false;
    private static boolean webFlag = false;

    @Autowired
    private ProfileRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebClient webClient;

    @Autowired
    public IProfileService(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public List<ProfileEntity> getProfiles() {
        // 1. Intentar actualizar/obtener datos si no se ha hecho en esta ejecución
        if (!savedFlag) {
            getListProfiles();
        }

        // 2. Rescatar la data directamente desde nuestra base de datos local
        List<ProfileEntity> dbProfiles = repository.findAll();

        return dbProfiles;
    }

    @Override
    public Optional<ProfileEntity> getProfile(int id) {
        if (!savedFlag)
            getListProfiles();

        Optional<ProfileEntity> dbProfile = repository.findById(id);

        return dbProfile;
    }

    private void getListProfiles() {
        List<DiputadoDTO> listDiputados = getListClass(uriDiputadosScraper, DiputadoDTO.class);
        List<DistritoDTO> listDistritos = getListClass(uriDistritosScraper, DistritoDTO.class);

        if (listDiputados == null || listDistritos == null) {
            webFlag = true;
            return;
        }

        List<ProfileDTO> listProfiles = getListProfileWithData(listDiputados, listDistritos);
        List<ProfileEntity> listEntity = getListEntityFromDTO(listProfiles);

        repository.saveAll(listEntity);
        repository.flush();
    }

    private List<ProfileEntity> getListEntityFromDTO(List<ProfileDTO> listProfiles) {
        List<ProfileEntity> listEntity = new ArrayList<>();

        for (ProfileDTO p : listProfiles) {
            String regionStr = (p.getRegion() != null) ? p.getRegion().nombre() : null;

            List<String> comunasStr = new ArrayList<>();
            if (p.getComunas() != null) {
                for (ComunaDTO comuna : p.getComunas()) {
                    comunasStr.add(comuna.nombre());
                }
            }

            // Evitamos registrar datos si el ID es nulo por seguridad
            if (p.getId() != null) {
                listEntity.add(new ProfileEntity(
                        p.getId(),
                        p.getNombreCompleto(),
                        p.getPartido(),
                        p.getDistrito() != null ? p.getDistrito() : 0,
                        regionStr,
                        comunasStr
                ));
            }
        }

        if (!listEntity.isEmpty())
            savedFlag = true;

        return listEntity;
    }

    private DistritoDTO getLocation(List<DistritoDTO> listDistritos, Integer idDistrito) {
        if (idDistrito == null) return null;
        for (DistritoDTO distrito : listDistritos) {
            if (distrito.id().equals(idDistrito)) {
                return distrito;
            }
        }
        return null;
    }

    private List<ProfileDTO> getListProfileWithData(List<DiputadoDTO> listDiputados, List<DistritoDTO> listDistritos) {
        List<ProfileDTO> profiles = new ArrayList<>();

        for (DiputadoDTO diputado : listDiputados) {
            ProfileDTO profileBuffer = new ProfileDTO(
                    diputado.id(),
                    diputado.nombreCompleto(),
                    diputado.aliasPartido(),
                    diputado.distrito()
            );

            DistritoDTO locationBuffer = getLocation(listDistritos, diputado.distrito());

            if (locationBuffer != null) {
                if (locationBuffer.region() != null)
                    profileBuffer.setRegion(locationBuffer.region());
                if (locationBuffer.comunas() != null)
                    profileBuffer.setComunas(locationBuffer.comunas());
            }

            profiles.add(profileBuffer);
        }

        return profiles;
    }

    private <T> List<T> getListClass(String uri, Class<T> elementClass) {
        try {
            String jsonResponse = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                return null;
            }

            // Parseamos a String para JsonNode manualmente
            JsonNode root = objectMapper.readTree(jsonResponse);
            List<T> classList = new ArrayList<>();

            // 1. Evaluar formato estándar de CollectionModel ("content")
            if (root.has("content") && root.get("content").isArray()) {
                for (JsonNode node : root.get("content")) {
                    classList.add(objectMapper.treeToValue(node, elementClass));
                }
            }
            // 2. Evaluar formato HAL ("_embedded")
            else if (root.has("_embedded")) {
                JsonNode embedded = root.get("_embedded");
                Iterator<JsonNode> fields = embedded.elements();
                if (fields.hasNext()) {
                    JsonNode arrayNode = fields.next();
                    if (arrayNode.isArray()) {
                        for (JsonNode node : arrayNode) {
                            classList.add(objectMapper.treeToValue(node, elementClass));
                        }
                    }
                }
            }
            // 3. Evaluar formato de Arreglo simple JSON
            else if (root.isArray()) {
                for (JsonNode node : root) {
                    classList.add(objectMapper.treeToValue(node, elementClass));
                }
            }

            if (classList.isEmpty()) {
                return null;
            }

            System.out.println("|diputado_profile_service|Mensaje: Conexión exitosa y datos mapeados desde " + uri);
            webFlag = false;
            return classList;

        } catch (Exception e) {
            System.err.println("|diputado_profile_service|Aviso: Falla de conversión o conexión en " + uri + " | Motivo: " + e.getMessage());
            return null;
        }
    }
}