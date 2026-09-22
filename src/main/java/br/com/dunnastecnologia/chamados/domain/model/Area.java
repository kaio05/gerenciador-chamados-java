package br.com.dunnastecnologia.chamados.domain.model;

import br.com.dunnastecnologia.chamados.domain.validation.ValidationLimits;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "areas")
@Getter
@Setter
public class Area {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = ValidationLimits.AREA_IDENTIFICACAO_MAX_LENGTH)
    private String identificacao;

    @OneToMany(mappedBy = "area")
    private List<Reserva> reservas;
    
}
