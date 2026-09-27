package br.com.dunnastecnologia.chamados.application.UserCase;

import br.com.dunnastecnologia.chamados.application.Security.AuthenticatedUser;
import br.com.dunnastecnologia.chamados.application.pagination.PageResult;
import br.com.dunnastecnologia.chamados.domain.model.Reserva;

import java.util.UUID;

import org.springframework.data.domain.PageRequest;


public interface ReservaUseCase {

    PageResult<Reserva> listarReservasDoMorador(
            AuthenticatedUser morador,
            PageRequest pageRequest
    );

    void rejeitarReserva(AuthenticatedUser admin, UUID reservaId, String motivo);

    void cancelarReserva(AuthenticatedUser morador, UUID reservaId);

    void aprovarReserva(AuthenticatedUser morador, UUID reservaId);
}
