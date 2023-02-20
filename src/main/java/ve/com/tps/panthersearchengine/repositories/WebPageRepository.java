package ve.com.tps.panthersearchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ve.com.tps.panthersearchengine.entities.WebPage;

import java.util.List;

//REPOSITORIO DE WEB PAGE
public interface WebPageRepository extends JpaRepository<WebPage,Integer> {

    //OBTIENE TODOS LOS OBJETOS WEBPAGE CUYA DESCRIPCIÓN CONTENGA LO ENVIADO COMO PARÁMETRO
    List<WebPage> findByDescriptionContaining(String textSearch);

    //BUSCAMOS LA WEBPAGE SEGÚN SU URL
    WebPage findByUrl(String url);

    //CREAMOS UN QUERY PARA OBTENER LOS WEB PAGE CUYA DESCRIPCIÓN SEA NULA Y SU TITULO SEA NULO
    //LE INDICAMOS NATIVE QUERY PARA INDICARLE AL REPOSITORIO QUE NO SERÁ UNA PETICIÓN JPA SINO SQL NATIVO.
    @Query(value="select * FROM webpage WHERE description IS null AND title IS null",nativeQuery = true)
    List<WebPage> findByDescriptionAndTitleIsNull();
}

