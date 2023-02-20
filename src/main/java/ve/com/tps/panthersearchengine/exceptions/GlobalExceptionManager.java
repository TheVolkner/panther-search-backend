package ve.com.tps.panthersearchengine.exceptions;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ve.com.tps.panthersearchengine.dto.ErrorDetallesDTO;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
//ESTA CLASE NOS SERVIRÁ PARA CONTROLAR LAS EXCEPCIONES QUE SE DISPAREN EN LA APLICACIÓN, Y ENVIAR UNA RESPUESTA JSON AL CLIENTE.
public class GlobalExceptionManager extends ResponseEntityExceptionHandler {

    //INTERCEPTAMOS LA EXEPCIÓN, GENERAMOS UN OBJETO ERRORDETALLES, LE INDICAMOS LA FECHA, EL MENSAJE DE LA EXCEPCIÓN Y EL URI DE LA PETICIÓN WEB

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetallesDTO> manejarResourceNotFoundException(ResourceNotFoundException resource, WebRequest webRequest){

        ErrorDetallesDTO detalles = new ErrorDetallesDTO(LocalDateTime.now(),resource.getMessage(),webRequest.getDescription(false));

        return new ResponseEntity<>(detalles, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetallesDTO> manejarGlobalException(Exception resource, WebRequest webRequest) {

        ErrorDetallesDTO detalles = new ErrorDetallesDTO(LocalDateTime.now(), resource.getMessage(), webRequest.getDescription(false));

        return new ResponseEntity<>(detalles, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    //MANEJO DE ARGUMENTOS NO VÁLIDOS EN LAS PETICIONES HTTP
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        //CREAMOS UN MAPA PARA GUARDAR POR NOMBRE Y VALOR CADA ARGUMENTO NO VÁLIDO
        Map<String,String> errores = new HashMap<>();

        //OBTENEMOS DE LA EXCEPCIÓN LA LISTA DE ERRORES, LOS ITERAMOS Y OBTENEMOS EL CAMPO DE ERROR Y EL MENSAJE DEL MISMO
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String nombreError = ((FieldError) error).getField();
            String mensajeError = error.getDefaultMessage();
            //LOS COLOCAMOS EN LA LISTA
            errores.put(nombreError,mensajeError);
        });
        // Y DEVOLVEMOS
        return new ResponseEntity<>(errores,HttpStatus.BAD_REQUEST);
    }

}