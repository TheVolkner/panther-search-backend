package ve.com.tps.panthersearchengine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebPageDTO {

    private Integer idWebPage;
    private String url;
    private String title;
    private String description;

    public WebPageDTO(String url){

        this.url = url;
    }
}
