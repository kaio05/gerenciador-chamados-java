package br.com.dunnastecnologia.chamados.infrastructure.repository;

import br.com.dunnastecnologia.chamados.domain.model.Reserva;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, UUID> {

    Page<Reserva> findByMoradorId(
        UUID moradorId,
        Pageable pageable
    );

    @Query(value = """
    SELECT r.*
    FROM reservas r
    WHERE r.area_id = :areaId
      AND r.status = 'APROVADA'::reserva_status
      AND r.data = :date
      AND r.horario_inicio < :horarioFim
      AND r.horario_fim > :horarioInicio
    """,
    countQuery = """
    SELECT COUNT(r.id)
    FROM reservas r
    WHERE r.area_id = :areaId
      AND r.status = 'APROVADA'::reserva_status
      AND r.data = :date
      AND r.horario_inicio < :horarioFim
      AND r.horario_fim > :horarioInicio
    """,
    nativeQuery = true)
    Optional<Reserva> buscarReservaConcorrente(
            @Param("areaId") UUID areaId,
            @Param("date") LocalDate data,
            @Param("horarioInicio") LocalTime horarioInicio,
            @Param("horarioFim") LocalTime horarioFim
    );

}
