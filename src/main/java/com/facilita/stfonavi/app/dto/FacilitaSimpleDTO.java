package com.facilita.stfonavi.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacilitaSimpleDTO {

    @NotBlank(message = "El campo 'nombre' no puede estar vacío")
    private String nombre;

}
