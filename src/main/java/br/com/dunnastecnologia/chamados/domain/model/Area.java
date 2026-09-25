package br.com.dunnastecnologia.chamados.domain.model;

import br.com.dunnastecnologia.chamados.domain.validation.ValidationLimits;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
    
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "area_status")
    private AreaStatus status;
    
    public enum AreaStatus {
    ATIVA, DESATIVADA, RETIRADA;
    }
}
