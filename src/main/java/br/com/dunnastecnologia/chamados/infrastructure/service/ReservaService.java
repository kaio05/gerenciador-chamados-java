package br.com.dunnastecnologia.chamados.infrastructure.service;

import br.com.dunnastecnologia.chamados.application.Security.AuthenticatedUser;
import br.com.dunnastecnologia.chamados.application.UserCase.ReservaUseCase;
import br.com.dunnastecnologia.chamados.application.pagination.PageResult;
import br.com.dunnastecnologia.chamados.domain.model.Area;
import br.com.dunnastecnologia.chamados.domain.model.Reserva;
import br.com.dunnastecnologia.chamados.infrastructure.exception.BusinessRuleException;
import br.com.dunnastecnologia.chamados.infrastructure.exception.ResourceNotFoundException;
import br.com.dunnastecnologia.chamados.infrastructure.repository.AreaRepository;
import br.com.dunnastecnologia.chamados.infrastructure.repository.MoradorRepository;
import br.com.dunnastecnologia.chamados.infrastructure.repository.ReservaRepository;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.AuthenticatedUserValidator;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.PageResultMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReservaService implements ReservaUseCase {

    private final ReservaRepository reservaRepository;
    private final AreaRepository areaRepository;
    private final MoradorRepository moradorRepository;
    private final AuthenticatedUserValidator authenticatedUserValidator;

    public ReservaService(
            ReservaRepository reservaRepository,
            AuthenticatedUserValidator authenticatedUserValidator,
            AreaRepository areaRepository,
            MoradorRepository moradorRepository
    ) {
        this.reservaRepository = reservaRepository;
        this.areaRepository = areaRepository;
        this.moradorRepository = moradorRepository;
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

    public Reserva cadastrarReserva(AuthenticatedUser morador, UUID areaId, LocalDate data, LocalTime horarioInicio, LocalTime horarioFim) {
        authenticatedUserValidator.assertMorador(morador);
        LocalDateTime reservaInicio = LocalDateTime.of(data, horarioInicio);
        LocalDateTime reservaFim = LocalDateTime.of(data, horarioFim);

        if(reservaInicio.isBefore(LocalDateTime.now()) || reservaInicio.isAfter(reservaFim))
            throw new BusinessRuleException("Insira um intervalo de tempo válido.");
        Area area = areaRepository.findById(areaId)
        .orElseThrow(() -> new IllegalArgumentException("Área não encontrada"));
        if(area.getStatus() == Area.AreaStatus.DESATIVADA || area.getStatus() == Area.AreaStatus.RETIRADA)
            throw new BusinessRuleException("Morador não pode solicitar reserva para uma area desativada ou retirada");
        Optional<Reserva> concorrente = reservaRepository.buscarReservaConcorrente(areaId, data, horarioInicio, horarioFim);
        if(concorrente.isPresent()) throw new BusinessRuleException("Morador não pode solicitar reserva para uma area ja reservada");

        Reserva reserva = new Reserva();
        reserva.setArea(areaRepository.getReferenceById(areaId));
        reserva.setData(data);
        reserva.setMorador(moradorRepository.getReferenceById(morador.id()));
        reserva.setHorarioInicio(horarioInicio);
        reserva.setHorarioFim(horarioFim);
        reserva.setStatus(Reserva.ReservaStatus.SOLICITADA);

        Reserva savedReserva = reservaRepository.save(reserva);
        return reservaRepository.findById(savedReserva.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Reserva nao encontrada apos cadastro"));
    }

    @Override
    @Transactional
    public void rejeitarReserva(AuthenticatedUser admin, UUID reservaId) {
        authenticatedUserValidator.assertAdministrador(admin);
        Reserva reserva = reservaRepository.getReferenceById(reservaId);
        reserva.setStatus(Reserva.ReservaStatus.REJEITADA);
        reservaRepository.save(reserva);
    }

    @Override
    @Transactional
    public void cancelarReserva(AuthenticatedUser morador, UUID reservaId) {
        authenticatedUserValidator.assertMorador(morador);
        Reserva reserva = reservaRepository.getReferenceById(reservaId);
        reserva.setStatus(Reserva.ReservaStatus.CANCELADA);
        reservaRepository.save(reserva);
    }

    @Override
    @Transactional
    public void aprovarReserva(AuthenticatedUser admin, UUID reservaId) {
        authenticatedUserValidator.assertAdministrador(admin);
        Reserva reserva = reservaRepository.getReferenceById(reservaId);
        if(reserva.getStatus() == Reserva.ReservaStatus.CANCELADA) throw new BusinessRuleException("Não se pode aprovar uma reserva cancelada");
        Optional<Reserva> concorrente = reservaRepository.buscarReservaConcorrente(reserva.getArea().getId(), reserva.getData(), reserva.getHorarioInicio(), reserva.getHorarioFim());
        if(concorrente.isPresent()) throw new BusinessRuleException("Não se pode aprovar uma reserva para uma área já reservada");
        reserva.setStatus(Reserva.ReservaStatus.APROVADA);
        reservaRepository.save(reserva);
    }
}
