package br.com.dunnastecnologia.chamados.infrastructure.controller.web.form;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservaForm {

    private UUID areaId;
    private LocalDate data;
    private LocalTime horarioInicio;
    private LocalTime horarioFim;
}
