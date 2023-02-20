package ve.com.tps.panthersearchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ve.com.tps.panthersearchengine.dto.WebPageDTO;
import ve.com.tps.panthersearchengine.exceptions.ResourceNotFoundException;
import ve.com.tps.panthersearchengine.services.WebPageService;
import ve.com.tps.panthersearchengine.services.impl.SpiderService;

import java.util.List;

@RestController
@RequestMapping("/api/webpages")
@CrossOrigin("*")
public class WebPageController {

    //INYECTAMOS LOS SERVICIOS DEL WEB PAGE Y SPIDER
    @Autowired
    private WebPageService webPageService;

    @Autowired
    private SpiderService spiderService;

    //HACEMOS UNA BUSQUEDA DE WEB PAGES CUYA DESCRIPCIÓN CONTENGA LO INDICADO EN EL PATH VARIABLE
    //ESTE MÉTODO ES EL QUE SE COMUNICARÁ CON EL CLIENTE
    @GetMapping("/buscar/{descripcion}")
    public ResponseEntity<List<WebPageDTO>> listarPaginasSegunDescripcion(@PathVariable String descripcion) throws ResourceNotFoundException{

        List<WebPageDTO> paginas = webPageService.search(descripcion);

        if(paginas != null && paginas.size() > 0){

            return new ResponseEntity<>(paginas,HttpStatus.OK);
        } else {
            throw new ResourceNotFoundException();
        }
    }

    //LISTAMOS LAS PÁGINAS DE LA BBDD
    //ESTE MÉTODO ES UTILIZADO SOLO EN PRUEBAS INTERNAS
    @GetMapping("/listar")
    public ResponseEntity<List<WebPageDTO>> listarWebPages() throws ResourceNotFoundException {

        List<WebPageDTO> listaWebPages = webPageService.BuscarPaginas();

        if(listaWebPages != null && listaWebPages.size() > 0){

            return new ResponseEntity<>(listaWebPages, HttpStatus.OK);
        } else {

            throw new ResourceNotFoundException();
        }
    }

    //AGREGAR UNA WEB PAGE SOLO CON SU URL A LA BBDD PARA HACER INDEXING
    //ESTE MÉTODO ES UTILIZADO SOLO EN PRUEBAS INTERNAS
    @PostMapping("/agregar")
    public ResponseEntity<Void> agregarWebPage(@RequestBody WebPageDTO webPageDTO) throws ResourceNotFoundException{

        webPageService.save(webPageDTO);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    //COMENZAR EL WEB SPIDER Y EL INDEXING DE LAS URLS DE LA BBDD
    //ESTE MÉTODO ES UTILIZADO SOLO EN PRUEBAS INTERNAS
   @GetMapping("/spider")
    public ResponseEntity<Void> iniciarSpider(){
        spiderService.startIndexing();

       return new ResponseEntity<>(HttpStatus.OK);
   }





}
