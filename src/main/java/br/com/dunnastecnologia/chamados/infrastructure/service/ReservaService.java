package br.com.dunnastecnologia.chamados.infrastructure.service;

import br.com.dunnastecnologia.chamados.application.Security.AuthenticatedUser;
import br.com.dunnastecnologia.chamados.application.UserCase.ReservaUseCase;
import br.com.dunnastecnologia.chamados.application.pagination.PageResult;
import br.com.dunnastecnologia.chamados.domain.model.Area;
import br.com.dunnastecnologia.chamados.domain.model.Reserva;
import br.com.dunnastecnologia.chamados.domain.validation.ValidationLimits;
import br.com.dunnastecnologia.chamados.infrastructure.exception.BusinessRuleException;
import br.com.dunnastecnologia.chamados.infrastructure.exception.ResourceNotFoundException;
import br.com.dunnastecnologia.chamados.infrastructure.repository.AreaRepository;
import br.com.dunnastecnologia.chamados.infrastructure.repository.MoradorRepository;
import br.com.dunnastecnologia.chamados.infrastructure.repository.ReservaRepository;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.AuthenticatedUserValidator;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.InputValidationSupport;
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
            throw new BusinessRuleException("Insira um intervalo de tempo valido.");
        Area area = areaRepository.findById(areaId)
        .orElseThrow(() -> new IllegalArgumentException("Área nao encontrada"));
        if(area.getStatus() == Area.AreaStatus.DESATIVADA || area.getStatus() == Area.AreaStatus.RETIRADA)
            throw new BusinessRuleException("Morador nao pode solicitar reserva para uma area desativada ou retirada");
        Optional<Reserva> concorrente = reservaRepository.buscarReservaConcorrente(areaId, data, horarioInicio, horarioFim);
        if(concorrente.isPresent()) throw new BusinessRuleException("Morador nao pode solicitar reserva para uma area ja reservada");

        Reserva reserva = new Reserva();
        reserva.setArea(areaRepository.getReferenceById(areaId));
        reserva.setData(data);
        reserva.setMorador(moradorRepository.getReferenceById(morador.id()));

        if(!reserva.getMorador().getAtivo()) throw new BusinessRuleException("Um morador inativo nao pode fazer uma reserva");

        reserva.setHorarioInicio(horarioInicio);
        reserva.setHorarioFim(horarioFim);
        reserva.setStatus(Reserva.ReservaStatus.SOLICITADA);

        Reserva savedReserva = reservaRepository.save(reserva);
        return reservaRepository.findById(savedReserva.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Reserva nao encontrada apos cadastro"));
    }

    @Override
    @Transactional
    public void rejeitarReserva(AuthenticatedUser admin, UUID reservaId, String motivo) {
        authenticatedUserValidator.assertAdministrador(admin);
        String motivoNormalizado = InputValidationSupport.normalizeRequiredText(
            motivo,
            "Motivo da rejeicao e obrigatorio",
            "Motivo deve ter no maximo 255 caracteres",
            ValidationLimits.DEFAULT_TEXT_MAX_LENGTH
        );
        Reserva reserva = reservaRepository.getReferenceById(reservaId);
        reserva.setMotivoCancelamento(motivoNormalizado);
        reserva.setStatus(Reserva.ReservaStatus.REJEITADA);
        reservaRepository.save(reserva);
    }

    @Override
    @Transactional
    public void cancelarReservaMorador(AuthenticatedUser morador, UUID reservaId) {
        authenticatedUserValidator.assertMorador(morador);
        cancelarReserva(reservaId);
    }

    @Override
    @Transactional
    public void cancelarReservaAdmin(AuthenticatedUser admin, UUID reservaId) {
        authenticatedUserValidator.assertAdministrador(admin);
        cancelarReserva(reservaId);
    }

    @Transactional
    private void cancelarReserva(UUID reservaId) {
        Reserva reserva = reservaRepository.getReferenceById(reservaId);

        if(reserva.getStatus() == Reserva.ReservaStatus.REJEITADA) throw new BusinessRuleException("Nao se pode cancelar uma reserva rejeitada");

        LocalDateTime reservaInicio = LocalDateTime.of(reserva.getData(), reserva.getHorarioInicio());
        if(reservaInicio.isBefore(LocalDateTime.now()))
            throw new BusinessRuleException("O horario desta reserva ja expirou.");
        reserva.setStatus(Reserva.ReservaStatus.CANCELADA);
        reservaRepository.save(reserva);
    }

    @Override
    @Transactional
    public void aprovarReserva(AuthenticatedUser admin, UUID reservaId) {
        authenticatedUserValidator.assertAdministrador(admin);
        Reserva reserva = reservaRepository.getReferenceById(reservaId);

        LocalDateTime reservaInicio = LocalDateTime.of(reserva.getData(), reserva.getHorarioInicio());
        if(reservaInicio.isBefore(LocalDateTime.now()))
            throw new BusinessRuleException("O horario desta reserva ja expirou.");
        Area area = areaRepository.findById(reserva.getArea().getId())
        .orElseThrow(() -> new IllegalArgumentException("Área nao encontrada"));

        if(!reserva.getMorador().getAtivo()) throw new BusinessRuleException("Um morador inativo nao pode fazer uma reserva");

        if(area.getStatus() == Area.AreaStatus.DESATIVADA || area.getStatus() == Area.AreaStatus.RETIRADA)
            throw new BusinessRuleException("Morador nao pode solicitar reserva para uma area desativada ou retirada");

        if(reserva.getStatus() == Reserva.ReservaStatus.CANCELADA || reserva.getStatus() == Reserva.ReservaStatus.REJEITADA) throw new BusinessRuleException("Nao se pode aprovar uma reserva cancelada ou rejeitada");
        
        Optional<Reserva> concorrente = reservaRepository.buscarReservaConcorrente(reserva.getArea().getId(), reserva.getData(), reserva.getHorarioInicio(), reserva.getHorarioFim());
        if(concorrente.isPresent()) throw new BusinessRuleException("Nao se pode aprovar uma reserva para uma area ja reservada");

        reserva.setStatus(Reserva.ReservaStatus.APROVADA);
        reservaRepository.save(reserva);
    }
}
