package br.com.dunnastecnologia.chamados.infrastructure.service;

import br.com.dunnastecnologia.chamados.application.Security.AuthenticatedUser;
import br.com.dunnastecnologia.chamados.application.UserCase.ReservaUseCase;
import br.com.dunnastecnologia.chamados.application.pagination.PageResult;
import br.com.dunnastecnologia.chamados.domain.model.Reserva;
import br.com.dunnastecnologia.chamados.infrastructure.repository.ReservaRepository;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.AuthenticatedUserValidator;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.PageResultMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReservaService implements ReservaUseCase {

    private final ReservaRepository reservaRepository;
    private final AuthenticatedUserValidator authenticatedUserValidator;

    public ReservaService(
            ReservaRepository reservaRepository,
            AuthenticatedUserValidator authenticatedUserValidator
    ) {
        this.reservaRepository = reservaRepository;
        this.authenticatedUserValidator = authenticatedUserValidator;
    }

    @Override
    public PageResult<Reserva> listarReservasDoMorador(
		AuthenticatedUser morador,
		PageRequest pageRequest
    ) {
        authenticatedUserValidator.assertMorador(morador);
        return PageResultMapper.fromPage(
			reservaRepository.findByMoradorId(morador.id(), pageRequest)
        );
    }
}
