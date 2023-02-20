package ve.com.tps.panthersearchengine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ErrorDetallesDTO {

    private LocalDateTime marcaTiempo;
    private String mensaje;
    private String detalles;


}

