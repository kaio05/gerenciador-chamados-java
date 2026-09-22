package br.com.dunnastecnologia.chamados.domain.model;

import br.com.dunnastecnologia.chamados.domain.validation.ValidationLimits;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "reservas")
@Getter
@Setter
public class Reserva {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "morador_id")
    private Morador morador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id")
    private Area area;

    @Column(nullable = false)
    private LocalDateTime data;

    @Column(nullable = false)
    private Time horarioInicio;

    @Column(nullable = false)
    private Time horarioFim;

    @Enumerated(EnumType.STRING)
    private ReservaStatus status;
    
    private enum ReservaStatus {
    SOLICITADA, APROVADA, REJEITADA, CANCELADA;
    }
}
