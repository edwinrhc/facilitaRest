package com.facilita.stfonavi.app.rest;


import com.facilita.stfonavi.app.dto.FacilitaSimpleDTO;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;


import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/facilita")
public class FacilitaRest {

    private static final Logger logger = LoggerFactory.getLogger(FacilitaRest.class);

    @Value("${facilita.auth.token}")
    private String facilitaAuthToken;

    @PostMapping("/nombre")
    public ResponseEntity<String> recibirNombre(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody FacilitaSimpleDTO payload
    ) {
        // 1) Validar que exista el header Authorization
        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Falta el header Authorization");
        }

        // 2) Extraer el token sin “Bearer ”
        String tokenRecibido = authHeader.replace("Bearer ", "").trim();

        // 3) Comparar con el token configurado en application.properties
        if (!tokenRecibido.equals(facilitaAuthToken)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Token inválido");
        }

        // 4) Si el token es correcto, procesamos el nombre
        String nombre = payload.getNombre();
        logger.info("→ Facilita envió nombre: {}", nombre);

        // 5) Devolver mensaje de confirmación
        return ResponseEntity.ok("Nombre recibido correctamente: " + nombre);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> manejarErroresValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(err -> {
            String campo = ((FieldError) err).getField();
            String msg   = err.getDefaultMessage();
            errores.put(campo, msg);
        });
        return ResponseEntity.badRequest().body(errores);
    }
}