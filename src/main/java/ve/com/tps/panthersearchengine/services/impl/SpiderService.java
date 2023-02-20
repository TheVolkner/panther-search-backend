package ve.com.tps.panthersearchengine.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ve.com.tps.panthersearchengine.dto.WebPageDTO;
import ve.com.tps.panthersearchengine.services.WebPageService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.hibernate.internal.util.StringHelper.isBlank;

//SERVICIO SPIDER PARA OBTENER LINKS DE CADA PÁGINA QUE TENGAMOS GUARDADA EN LA BBDD
@Service
public class SpiderService {

    //INYECTAMOS EL WEBPAGE SERVICE PARA PODER MANIPULAR LOS DATOS DE LOS OBJETOS WEBPAGE
    @Autowired
    private WebPageService webpageService;

    //ESTE MÉTODO SE ENCARGA DE COMENZAR EL INDEXING
    //PRIMERO SOLICITA TODOS LOS WEB PAGE CUYOS ATRIBUTOS TENGAN SU DESCRIPCIÓN Y SU TÍTULO NULOS
    //ESTOS SERÁN LOS LINKS QUE HABRÁN SIDO EXTRAIDOS DE OTRO INDEXING PREVIO
    //LO QUE HACE ES OBTENER EL TÍTULO Y LA DESCRIPCIÓN DE CADA URL, SI NO PUEDE LO ELIMINA
    public void startIndexing() {

        List<WebPageDTO> linksToIndex = webpageService.findWebPagesToIndex();
        linksToIndex.stream().parallel().forEach(webPage -> {
            try {
                indexWebPage(webPage);
            } catch (Exception e) {
                System.out.println("EXCEPTION IN THE PROCESS = ");
                System.out.println(e.getMessage());
                webpageService.removeLink(webPage);
                System.out.println("LINK DELETED...");
            }
        });

    }

    //COMIENZA EL PROCESO SOLICITANDO EL URL Y EL CONTENIDO DEL URL
    //LUEGO MANDA EL URL TERMINADO A GUARDAR Y DE ESE URL INTENTA OBTENER LINKS NUEVOS PARA INDEXAR LUEGO
    private void indexWebPage(WebPageDTO webPage) throws Exception{
        System.out.println("Indexing " + webPage.getUrl());
        String url = webPage.getUrl();
        String content = getWebContent(url);
        if(isBlank(content)){
            return;
        }
        IndexAndSaveWebPage(webPage, content);
        saveLinks(getDomain(url),content);
    }

    //OBTENEMOS EL DOMINIO DIVIDIENDO SEGÚN LA BARRA LATERAL EL LINK
    //LO QUE ESTÉ EN LA PRIMERA POSICIÓN DEL ARREGLO DEL SPLIT, SERÁ EL HTTP CON EL NOMBRE DEL DOMINIMO
    //LO QUE HACEMOS ES MODIFICARLO PARA EN VEZ DE TENER UNA SOLA BARRA, TENER DOS Y LO RETORNAMOS
    private synchronized String getDomain(String url) {

        System.out.println("Obteniendo Dominio...");
        String[] cont1 = url.split("/");
        return cont1[0] + "//" + cont1[2];
    }

    //OBTENEMOS LOS LINKS QUE SE PUEDAN EXTRAER DEL URL ACTUAL, PARA GUARDARLOS EN LA BBDD E INDEXARLOS LUEGO
    private synchronized void saveLinks(String domain,String content) {

        System.out.println("Saving Links...");
        List<String> links = getLinks(domain,content);

        //COMPROBAMOS QUE LOS LINKS OBTENIDOS NO EXISTAN YA PREVIAMENTE EN LA BBDD
        //DE SER ASÍ, LOS GUARDAMOS
        links.stream().
                filter(link -> !webpageService.findByURL(link)).
                forEach(link-> webpageService.save(new WebPageDTO(link)));
    }

    //MANDAMOS A OBTENER EL TÍTULO DE LA WEB PAGE Y SU DESCRIPCIÓN
    //SE LOS ASIGNAMOS AL OBJETO WEB PAGE Y LO GUARDAMOS EN LA BBDD
    private synchronized void IndexAndSaveWebPage(WebPageDTO wp, String content) {
        String title = getTitle(content);
        String description = getDescription(content);

        wp.setTitle(title);
        wp.setDescription(description);
        System.out.println("Saving Web Page...");
        webpageService.save(wp);
    }

    //OBTENEMOS LOS LINKS QUE ESTÉN DISPONIBLES EN ESTE URL ACTUAL
    public synchronized List<String> getLinks(String domain,String content){

        System.out.println("Getting links from the content...");
        List<String> links = new ArrayList<>();
        //LO QUE HAREMOS SERÁ DIVIDIR EL CONTENIDO DONDE ENCUENTRE UN HREF, EL CÚAL PODRIA SER UN LINK
        String[] cont1 = content.split("href=\"");

        //LO QUE OBTUVO DEL SPLIT FUE UN ARREGLO CON TODOS LOS PUNTOS DONDE COMIENZE UN HREF
        //LO TRANSFORMAMOS A LISTA PARA PODER MANEJARLO CON UN FOR EACH
        List<String> linksHTML = Arrays.asList(cont1);

        //HACEMOS UN FOR EACH PARA CICLAR CADA LINK OBTENIDO Y DIVIDIR EL LINK DONDE TERMINE EL HREF
        //OBTENIEDO DIRECTAMENTE EL LINK COMPLETO, Y PROCEDEMOS A AGREGARLO A LA LISTA DE LINKS
        linksHTML.forEach(link -> {

            //DIVIDIMOS EL STRING AHORA DONDE ENCUENTRE OTRA BARRA LATERAL, LA CÚAL INDICARÁ
            //EL FINAL DEL LINK, CON ESTO ESTARÍAMOS OBTENIENDO COMO PRIMER VALOR DEL ARREGLO CONT2
            //EL LINK COMPLETO Y LO AGREGAMOS A LA LISTA
            String[] cont2 = link.split("\"");
            links.add(cont2[0]);
        });
        System.out.println("Returning clean links...");
        //LUEGO MANDAMOS A FITLRAR ESOS LINKS
        return  cleanLinks(domain,links);
    }

    //ESTE MÉTODO SE ENCARGA DE ELIMINAR AQUELLOS LINKS QUE NO REDIRIGAN A UNA PÁGINA
    private synchronized List<String> cleanLinks(String domain,List<String> links) {

        System.out.println("Filtering links...");
        //GENERAMOS UN AREGLO STRING CON EXTENSIONES DE ARCHIVOS A EXCLUIR
        String[] excludedExtensions = {"css","js","png","jpg","gif","woff2","json"};

        //HACEMOS UN FLUJO DE LA LISTA DE EXTENSIONES A EXCLUIR, COMPROBANDO CÚALES LINKS NO TERMINEN
        //CON ESA EXTENSIÓN, LOS QUE NO SEAN DE ESOS TIPOS SE LES COMPROBARÁ A CONTINUACIÓN SI TIENEN
        //SU DOMINIMO, POR QUE SI COMIENZAN CON LA BARRA LATERAL HACE REFERENCIA A QUE ES UNA LLAMADA
        //A UNA PÁGINA INTERNA DE LA MISMA APLICACIÓN, ENTONCES LE AGREGAMOS SU DOMINIMO, DE POSEERLO
        //NO SE HACE NADA, LUEGO COMPROBAMOS QUE EL LINK NO COMIENCE CON # PORQUE ESO ES UN ANCLA A OTRA
        //SECCIÓN DEL MISMO URL, AL FINALIZAR DEVOLVEMOS LA LISTA cleanList
        List<String> cleanList = links.stream().filter(link -> Arrays.stream(excludedExtensions).
                        noneMatch(extension -> link.endsWith(extension))).
                map(link -> link.startsWith("/") ? domain + link : link).
                filter(link -> !link.startsWith("#")).
                collect(Collectors.toList());

        //ORDENA LA LISTA E IMPRIMIMOS EN CONSOLA LOS LINKS OBTENIDOS. AL FINAL RETORNAMOS
        List<String> filteredList = new ArrayList<>(new HashSet<String>(cleanList));
        filteredList.forEach(System.out::println);
        System.out.println("Returning filtered links...");
        return filteredList;
    }

    //BUSCA EN EL META DEL HTML, EL TAG DE TITLE Y LO DIVIDE EXTRAYENDO SU CONTENIDO
    public synchronized String getTitle(String content){

        System.out.println("Getting Title...");
        String[] cont1 = content.split("<title>");
        String[] cont2 = cont1[1].split("</title>");

        System.out.println("Returning Title...");
        return cont2[0];
    }

    //BUSCA EN EL META EL ATTRIBUTE DE DESCRIPTION, DIVIENDOLO PARA OBTENER SU CONTENIDO
    public synchronized String getDescription(String content){

        System.out.println("Getting Description...");
        String[] cont1 = content.split("<meta name=\"description\" content=\"");
        String[] cont2 = cont1[1].split("\" />");

        System.out.println("Returning Description..");
        return cont2[0];
    }

    //EL MÉTODO MÁS IMPORTANTE, REALIZA EL ANALISIS DEL URL Y EXTRAE SU CODIGO FUENTE
    public synchronized String getWebContent(String link) {

        System.out.println("INIT WEB SCRAPPER...");
        System.out.println(link);

        try {


            //PROCESO DE CONSTRUCCIÓN DEL WEB SCRAPPER, EN ESTE MÉTODO SE CREA UNO PARA CADA LINK.
            URL url = new URL(link);

            //GENERAMOS UNA CONECCIÓN AL URL
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            //OBTENEMOS EL CONTENG ENCODING
            String encode = conn.getContentEncoding();

            //OBTENEMOS EL FLUJO DE LA CONEXIÓN
            InputStream inputStream = conn.getInputStream();

            //A PARTIR DEL FLUJO RETORNAMOS UN BUFFERED READER QUE ES UN STRING DE VARIAS LINEAS
            //CON EL CODIGO DEL URL EXTRAIDO
            System.out.println("END OF WEB SCRAPPER. RETURNING CONTENT...");
            return new BufferedReader(new InputStreamReader(inputStream)).
                    lines().collect(Collectors.joining());

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return "";
    }
}