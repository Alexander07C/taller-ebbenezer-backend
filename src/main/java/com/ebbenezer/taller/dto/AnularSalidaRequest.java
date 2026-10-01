package com.ebbenezer.taller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnularSalidaRequest {

    @NotBlank
    private String motivoAnulacion;
}