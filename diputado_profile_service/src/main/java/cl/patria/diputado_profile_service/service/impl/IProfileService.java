package cl.patria.diputado_profile_service.service.impl;

import cl.patria.diputado_profile_service.model.ProfileEntity;
import cl.patria.diputado_profile_service.model.dto.DiputadoDTO;
import cl.patria.diputado_profile_service.model.dto.DistritoDTO;
import cl.patria.diputado_profile_service.model.dto.ProfileDTO;
import cl.patria.diputado_profile_service.repository.ProfileRepository;
import cl.patria.diputado_profile_service.service.ProfileService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class IProfileService implements ProfileService {

    private static boolean savedFlag = false;
    private static boolean webFlag = false;

    @Autowired
    private ProfileRepository repository;
    private final WebClient webClient;

    public IProfileService(WebClient webClient) {
        this.webClient = webClient;
    }

    /*Method retorna 'null' en caso que haya problemas en la comunicación con otros microservicios.
    * Si ya se ha guardado, no hacemos procedimiento nuevamente.
    * Si comunicación se resuelve, opera normalmente.
    * Tenemos 3 salidas con este metodo:
    *   1. Si falla comunicación, manejo del retorno NULL
    *   2. Si hay comunicación, manejo del retorno VACÍO
    *   3. Si hay comunicación, manejo del retorno DATOS*/
    @Override
    public List<ProfileEntity> getProfiles() {
        if (!savedFlag)
            getListProfiles();
        return (!webFlag) ? repository.findAll() : null;
    }

    /*Method retorna el 'ProfileEntity' si es encontrado, sino retorna 'null'.
    * Si ya se ha guardado, no hacemos procedimiento nuevamente.*/
    @Override
    public Optional<ProfileEntity> getProfile(int id) {
        if (!savedFlag)
            getListProfiles();
        return repository.findById(Integer.valueOf(id));
    }



    /*++++++++++++++++MÉTODOS PRIVADOS DE SERVICE+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

    /*Method 'padre', se encarga de obtener listado de Diputados y Distritos.
     * Se ejecuta automáticamente al iniciar el servidor.
     * Con listado, se encarga de dar sentido a la data con ambos listados.
     * Cada Diputado con su Distrito a representar.
     * Retorna el listado.*/
    @EventListener(ApplicationReadyEvent.class)
    protected void getListProfiles() {
        List<DiputadoDTO> listDiputados = getListClass(uriDiputadosScraper, DiputadoDTO.class);
        List<DistritoDTO> listDistritos = getListClass(uriDistritosScraper, DistritoDTO.class);

        if (listDiputados == null || listDistritos == null){
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
        for (ProfileDTO p : listProfiles)
            listEntity.add(new ProfileEntity(
                    p.getId(), p.getNombreCompleto(), p.getDistrito()));

        if (!listEntity.isEmpty())
            savedFlag = true;
        return listEntity;
    }

    /*Method busca el distrito del diputado
     * Si lo encuentra, retorna el distrito.*/
    private DistritoDTO getLocation(List<DistritoDTO> listDistritos, Integer idDistrito) {
        for (DistritoDTO distrito : listDistritos)
            if (distrito.id().equals(idDistrito))
                return distrito;
        return null;
    }

    /*Method recorre Diputados y Distritos recibidos con datos.
     * Con datos, da forma al perfil de cada diputado y distrito que representa.
     * Obtenemos cada diputado con cada distrito que representa*/
    private List<ProfileDTO> getListProfileWithData(List<DiputadoDTO> listDiputados, List<DistritoDTO> listDistritos) {
        List<ProfileDTO> profiles = new ArrayList<>();

        // Si Diputado está vacío, naturalmente no debería recorrer y retorna inmediatamente
        // profiles con estado vacío.
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

    /*Method retorna listado de Clase proporcionada.
     *Si al obtener lista desde webClient está vacía o si el servicio está caído, retornamos NULL.*/
    private <T> List<T> getListClass(String uri, Class<T> elementClass) {
        try {
            List<T> classList = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToFlux(elementClass)
                    .collectList()
                    .block();

            if (classList == null || classList.isEmpty()) {
                return null;
            }
            System.out.println("|diputado_profile_service|Mensaje: Se logró conectar con el microservicio " + elementClass.getCanonicalName() + " en " + uri);

            webFlag = false;
            return classList;

        } catch (Exception e) {
            // Capturamos el error de conexión para evitar que la app se caiga.
            System.err.println("|diputado_profile_service|Aviso: No se pudo conectar con el microservicio en " + uri + " | Motivo: " + e.getMessage());
            return null;
        }
    }
}