package ve.com.tps.panthersearchengine.services.impl;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ve.com.tps.panthersearchengine.dto.WebPageDTO;
import ve.com.tps.panthersearchengine.entities.WebPage;
import ve.com.tps.panthersearchengine.repositories.WebPageRepository;
import ve.com.tps.panthersearchengine.services.WebPageService;

import java.util.ArrayList;
import java.util.List;

//IMPLEMENTACIÓN DE LOS MÉTODOS DE SERVICIO CON CONEXIÓN A WEB PAGE REPOSITORY
@Service
public class WebPageServiceImpl implements WebPageService {


    //MODEL MAPPER PERMITE HACER LAS TRANSFORMACIONES ENTIDAD/DTO Y VICEVERSA RAPIDAMENTE.
    @Autowired
    private ModelMapper modelMapper;


    //INYECTAMOS EL REPOSITORIO WEB PAGES
    @Autowired
    private WebPageRepository webPageRepository;

    //BUSCAMOS TODAS LAS PÁGINAS
    public List<WebPageDTO> BuscarPaginas(){

        //OBTENEMOS LA LISTA CON LOS OBJETOS DE ENTIDAD
        List<WebPage> listaEntity = webPageRepository.findAll();

        //CREAMOS LA LISTA CON LOS OBJETOS DTO
        List<WebPageDTO> listaDTO = new ArrayList<>();

        //TRANSFORMAMOS CADA OBJETO DE ENTIDAD A DTO Y LO AÑADIMOS A LA LISTA
        listaEntity.forEach(webpage -> {
            listaDTO.add(modelMapper.map(webpage,WebPageDTO.class));
        });

        return listaDTO;
    }

    //BUSCAMOS UNA PÁGINA CUYA DESCRIPCIÓN COINCIDA CON LA BUSCADO
    public List<WebPageDTO> search(String textSearch){

        //OBTENEMOS LA LISTA CON LOS OBJETOS DE ENTIDAD
        List<WebPage> listaEntity = webPageRepository.findByDescriptionContaining(textSearch);;

        //CREAMOS LA LISTA CON LOS OBJETOS DTO
        List<WebPageDTO> listaDTO = new ArrayList<>();

        //TRANSFORMAMOS CADA OBJETO DE ENTIDAD A DTO Y LO AÑADIMOS A LA LISTA
        listaEntity.forEach(webpage -> {
            listaDTO.add(modelMapper.map(webpage,WebPageDTO.class));
        });

        return listaDTO;
    }

    //GUARDAMOS UNA PÁGINA
    public void save(WebPageDTO wp){

        //TRANSFORMAMOS EL OBJETO DTO A ENTIDAD
        WebPage wpToSave = modelMapper.map(wp,WebPage.class);

        //GUARDAMOS
        webPageRepository.save(wpToSave);
    }

    //COMPROBAMOS QUE LA PÁGINA A BUSCAR EXISTE EN LA BBDD
    public boolean findByURL(String url) {
        WebPage wpSearched = webPageRepository.findByUrl(url);

        return wpSearched != null;
    }

    //BUSCAMOS LAS PÁGINAS A INDEXAR EN EL SPIDER
    public List<WebPageDTO> findWebPagesToIndex(){

        //OBTENEMOS LA LISTA CON LOS OBJETOS DE ENTIDAD
        List<WebPage> listaEntity = webPageRepository.findByDescriptionAndTitleIsNull();;

        //CREAMOS LA LISTA CON LOS OBJETOS DTO
        List<WebPageDTO> listaDTO = new ArrayList<>();

        //TRANSFORMAMOS CADA OBJETO DE ENTIDAD A DTO Y LO AÑADIMOS A LA LISTA
        listaEntity.stream().forEach(webpage -> {
            listaDTO.add(modelMapper.map(webpage,WebPageDTO.class));
        });

        return listaDTO;

    }

    //ELIMINAMOS LAS PÁGINAS CUYOS LINKS NO SE PUEDAN PROCESAR
    public void removeLink(WebPageDTO webPage) {

        //TRANSFORMAMOS EL OBJETO DTO A ENTIDAD
        WebPage wpToDelete = modelMapper.map(webPage,WebPage.class);

        //ELIMINAMOS
        webPageRepository.delete(wpToDelete);
    }
}
