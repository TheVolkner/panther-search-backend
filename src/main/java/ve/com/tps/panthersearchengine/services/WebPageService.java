package ve.com.tps.panthersearchengine.services;

import ve.com.tps.panthersearchengine.dto.WebPageDTO;
import ve.com.tps.panthersearchengine.entities.WebPage;

import java.util.List;

//INTERFAZ CON LOS MÉTODOS DE SERVICIO IMPLEMENTADO PARA CONEXIONES SQL
public interface WebPageService {

    public List<WebPageDTO> BuscarPaginas();

    public List<WebPageDTO> search(String textSearch);

    public void save(WebPageDTO wp);

    public boolean findByURL(String url);

    public List<WebPageDTO> findWebPagesToIndex();

    public void removeLink(WebPageDTO webPage);
}
